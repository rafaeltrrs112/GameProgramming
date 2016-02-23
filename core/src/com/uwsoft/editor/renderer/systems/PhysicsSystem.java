package com.uwsoft.editor.renderer.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.PolygonComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.physics.PhysicsBodyComponent;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

public class PhysicsSystem extends IteratingSystem {

	protected ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);

	private final float TIME_STEP = 1f/60;
	private World world;
	private boolean isPhysicsOn = true;
	private float accumulator = 0;

	public PhysicsSystem(World world) {
		super(Family.all(PhysicsBodyComponent.class).get());
		this.world = world;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TransformComponent transformComponent =  transformComponentMapper.get(entity);

		processBody(entity);

		PhysicsBodyComponent physicsBodyComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
		Body body = physicsBodyComponent.body;
		transformComponent.x = body.getPosition().x/ PhysicsBodyLoader.getScale();
		transformComponent.y = body.getPosition().y/ PhysicsBodyLoader.getScale();
//		transformComponent.rotation = body.getAngle() * MathUtils.radiansToDegrees;

		if (world != null && isPhysicsOn) {
			doPhysicsStep(deltaTime);
		}
	}

	protected void processBody(Entity entity) {
		PhysicsBodyComponent physicsBodyComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
		PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

		if(polygonComponent == null && physicsBodyComponent.body != null) {
			world.destroyBody(physicsBodyComponent.body);
			physicsBodyComponent.body = null;
		}

		if(physicsBodyComponent.body == null && polygonComponent != null) {
			if(polygonComponent.vertices == null) return;

            DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);

            physicsBodyComponent.centerX = dimensionsComponent.width/2;
            physicsBodyComponent.centerY = dimensionsComponent.height/2;

			PhysicsBodyComponent bodyPropertiesComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
			physicsBodyComponent.body = PhysicsBodyLoader.getInstance().createBody(world, bodyPropertiesComponent, polygonComponent.vertices, new Vector2(transformComponent.scaleX, transformComponent.scaleY),
                    transformComponent.rotation * MathUtils.degreesToRadians);

            physicsBodyComponent.body.setTransform(new Vector2(transformComponent.x * PhysicsBodyLoader.getScale(), transformComponent.y * PhysicsBodyLoader.getScale()), physicsBodyComponent.body.getAngle());
			physicsBodyComponent.body.setUserData(entity);
		}
	}

	private void doPhysicsStep(float deltaTime) {
		// fixed time step
		// max frame time to avoid spiral of death (on slow devices)
		float frameTime = Math.min(deltaTime, 0.25f);
		accumulator += frameTime;
		while (accumulator >= TIME_STEP) {
			world.step(TIME_STEP, 6, 2);
			accumulator -= TIME_STEP;
		}
	}

	public void setPhysicsOn(boolean isPhysicsOn) {
		this.isPhysicsOn = isPhysicsOn;
	}

}
