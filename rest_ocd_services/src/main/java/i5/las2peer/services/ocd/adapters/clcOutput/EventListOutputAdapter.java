package i5.las2peer.services.ocd.adapters.clcOutput;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.utils.CommunityEvent;
import i5.las2peer.services.ocd.utils.CommunityEventType;
import i5.las2peer.services.ocd.utils.CommunityLifeCycle;
import org.apache.commons.exec.ExecuteException;

import java.io.Writer;

public class EventListOutputAdapter extends AbstractClcOutputAdapter{

    public EventListOutputAdapter(Writer writer){
        this.setWriter(writer);
    }

    public EventListOutputAdapter() {
    }

    @Override
    public void writeClc(CommunityLifeCycle clc) throws AdapterException {
        try{
            writer.write("TIMESTAMP\tEVENT\t\tCOMMUNITIES\tNODES\n");
            for(CommunityEvent event: clc.getEvents()){
                writer.write(event.getDate() + "\t\t");

                if(event.getEventType() == 2){
                    writer.write(CommunityEventType.lookupType(event.getEventType()).getDisplayName() + "\t");
                }else{
                    writer.write(CommunityEventType.lookupType(event.getEventType()).getDisplayName() + "\t\t");
                }
                writer.write(event.getCommunitiesInvolved().toString() + "\t\t");
                writer.write(event.getNodesInvolved() + "\n");
            }
        }catch(Exception e){
            throw new AdapterException(e);
        }
        finally {
            try {
                writer.close();
            }catch (Exception e){

            }
        }
    }
}
