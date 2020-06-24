package chumbanotz.mutantbeasts.client.animationapi;

public class Transform {
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float offsetX;
    private float offsetY;
    private float offsetZ;

	public float getRotationX() {
		return this.rotationX;
	}

	public float getRotationY() {
		return this.rotationY;
	}

	public float getRotationZ() {
		return this.rotationZ;
	}

	public float getOffsetX() {
		return this.offsetX;
	}

	public float getOffsetY() {
		return this.offsetY;
	}

	public float getOffsetZ() {
		return this.offsetZ;
	}

	public void addRotation(float x, float y, float z) {
		this.rotationX += x;
		this.rotationY += y;
		this.rotationZ += z;
	}

	public void addOffset(float x, float y, float z) {
		this.offsetX += x;
		this.offsetY += y;
		this.offsetZ += z;
	}
}