package i5.las2peer.services.ocd.graphs;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.DocumentReadOptions;
import com.arangodb.model.DocumentUpdateOptions;
import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@IdClass(CustomGraphId.class)
public class CustomGraphTimed extends CustomGraph {

    public static final String startDateColumnName = "START_DATE";
    public static final String endDateColumnName = "END_DATE";

    @Column(name = startDateColumnName)
    private Date startDate = new Date(Long.MIN_VALUE);

    @Column(name = endDateColumnName)
    private Date endDate = new Date(Long.MAX_VALUE);

    //TODO: Is this even needed? Probably just remove if we simply use super()
    public CustomGraphTimed(){
        super();
    }

    public CustomGraphTimed(CustomGraph graph) {
        super(graph);

        if(graph instanceof CustomGraphTimed timedGraph) {
            startDate = timedGraph.startDate;
            endDate = timedGraph.endDate;
        }
    }

    public BaseDocument persist(ArangoDatabase db, String transId) throws InterruptedException {
        ArangoCollection collection = db.collection(collectionName);
        BaseDocument bd = super.persist(db, transId);
        //options for the transaction
        DocumentUpdateOptions updateOptions = new DocumentUpdateOptions().streamTransactionId(transId);

        bd.addAttribute(customGraphTypeColumnName, "TIMED");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        bd.addAttribute(startDateColumnName, dateFormat.format(this.startDate));
        bd.addAttribute(endDateColumnName, dateFormat.format(this.endDate));

        collection.updateDocument(this.getKey(), bd, updateOptions);

        return bd;
    }

    public static CustomGraphTimed load(String key, ArangoDatabase db, String transId) throws OcdPersistenceLoadException {
        CustomGraphTimed customGraphTimed = (CustomGraphTimed) CustomGraph.load(key, db, transId);
        //CustomGraphTimed customGraphTimed = new CustomGraphTimed(customGraph);

        ArangoCollection collection = db.collection(collectionName);
        DocumentReadOptions readOpt = new DocumentReadOptions().streamTransactionId(transId);
        BaseDocument bd = collection.getDocument(key, BaseDocument.class, readOpt);

        if(bd.getAttribute(startDateColumnName) == null) {
            throw new OcdPersistenceLoadException("Loaded timed custom graph has no/empty date attributes");
        }

        try {
            customGraphTimed.startDate = DateUtils.parseDate(bd.getAttribute(startDateColumnName).toString(), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
        }
        catch (ParseException e) {
            customGraphTimed.startDate = new Date(Long.MIN_VALUE);
        }
        try {
            customGraphTimed.endDate = DateUtils.parseDate(bd.getAttribute(endDateColumnName).toString(), "yyyy-MM-dd'T'HH:mm:ss.sssXXX","yyyy-MM-dd'T'HH:mm:ss.sss'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'Z'", "yyyy-MM-dd'T'HH:mm:ss.sss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd");
        }
        catch (ParseException e) {
            customGraphTimed.endDate = new Date(Long.MAX_VALUE);
        }

        return customGraphTimed;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
