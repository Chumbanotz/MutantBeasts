package chumbanotz.mutantbeasts.client.animationapi;

/** Implement this on an entity that needs to use the {@link Animator} */
public interface IAnimatedEntity {
	int getAnimationID();

	int getAnimationTick();
}