package i5.las2peer.services.ocd.graphs;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import y.geom.YPoint;

/**
 * Represents a point for visualization persistence purposes.
 * @author Sebastian
 *
 */
@Embeddable
public class PointEntity {
	
	/**
	 * The x-coordinate of the point.
	 */
	@Column
	private double x;
	
	/**
	 * The y-coordinate of the point.
	 */
	@Column
	private double y;
	
	/**
	 * Creates a new instance.
	 */
	protected PointEntity() {
	}
	
	/**
	 * Copy constructor.
	 * @param point The point to copy.
	 */
	protected PointEntity(YPoint point) {
		this.x = point.getX();
		this.y = point.getY();
	}
	
	/**
	 * Getter for the x-coordinate.
	 * @return The x-coordinate.
	 */
	protected double getX() {
		return x;
	}

	/**
	 * Setter for the x-coordinate.
	 * @param x The x-coordinate.
	 */
	protected void setX(double x) {
		this.x = x;
	}

	/**
	 * Getter for the y-coordinate.
	 * @return The y-coordinate.
	 */
	protected double getY() {
		return y;
	}

	/**
	 * Setter for the y-coordinate.
	 * @param y The y-coordinate.
	 */
	protected void setY(double y) {
		this.y = y;
	}

	/**
	 * Creates a YPoint corresponding to the point.
	 * @return The YPoint.
	 */
	protected YPoint createPoint() {
		return new YPoint(x, y);
	}
	
}
