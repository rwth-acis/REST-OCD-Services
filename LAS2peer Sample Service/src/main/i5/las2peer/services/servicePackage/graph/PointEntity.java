package i5.las2peer.services.servicePackage.graph;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import y.geom.YPoint;

@Embeddable
public class PointEntity {
	
	@Column
	private double x;
	
	@Column
	private double y;
	
	protected PointEntity() {
	}
	
	protected PointEntity(YPoint point) {
		this.x = point.getX();
		this.y = point.getY();
	}
	
	protected double getX() {
		return x;
	}

	protected void setX(double x) {
		this.x = x;
	}

	protected double getY() {
		return y;
	}

	protected void setY(double y) {
		this.y = y;
	}

	protected YPoint createPoint() {
		return new YPoint(x, y);
	}
	
}
