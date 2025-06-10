package com.test;

import com.crossly.coffee_engine.component.*;
import com.crossly.coffee_engine.core.*;
import com.crossly.coffee_engine.entity.*;
import com.crossly.util.FileUtil;
import org.joml.Vector3f;

import java.sql.Time;
import java.time.LocalTime;

public class CatLikeClock extends Application {

    private Entity clock;

    public CatLikeClock() {
        super(1280, 720, "Cat Like Coding's Clock Project");
        createContext();
        Framebuffer.setClearColor(0, .5f, 1);
        Framebuffer.setDepthTest(true);
    }

    public void onCreate() {
        // Loading resources
        Shader shader = new Shader(FileUtil.getFileString("shaders/shader.vert"), FileUtil.getFileString("shaders/color_shader.frag"));
        Mesh cylinder = new StaticMesh("models/Cylinder.obj");
        Mesh cube = new StaticMesh("models/Cube.obj");
        // Creating entities that will make use of resources
        Camera camera = new Camera(getWidth(), getHeight());
        camera.setFov(45);
        camera.getComponent(Transform.class).ifPresent(
                transform -> transform.setPosition(new Vector3f(0, 0, -15))
        );
        var renderCallback = new RenderCallback() {
            @Override
            public void invoke(Entity self) {
                var shader = self.getComponent(Shader.class).orElse(null);
                var transform = self.getComponent(Transform.class).orElse(null);
                var mesh = self.getComponent(StaticMesh.class).orElse(null);
                if (mesh == null || transform == null || shader == null) return;
                shader.setMatrix4f("uProjection", camera.getProjection3D());
                shader.setMatrix4f("uView", camera.getView());
                shader.setMatrix4f("uModel", transform.getModelMatrix());
                shader.setFloat3("color", ((ColoredEntity)self).color);
                mesh.render();
            }
        };
        clock = Entity.create();
        Entity clockFace = new ColoredEntity(new Vector3f(1), new Transform(new Vector3f(), new Vector3f(90, 0, 0), new Vector3f(10, .2f, 10)), cylinder, shader);
        clockFace.setRenderCallback(renderCallback);
        clock.addChild(clockFace);
        for (int i = 0; i < 12; i++) {
            Entity hourIndicator =
                    new ColoredEntity(
                            new Vector3f(73f / 255),
                            new Transform(new Vector3f(0, 4, -.25f).rotateZ((float)Math.toRadians(i * 30)), new Vector3f(0, 0, 30 * i), new Vector3f(.5f, 1, .1f)),
                            cube, shader
                    );
            hourIndicator.setRenderCallback(renderCallback);
            clock.addChild(hourIndicator);
        }

        Entity hourArm = new ColoredEntity(new Vector3f(), new Transform(new Vector3f(0, .75f, -.25f), new Vector3f(), new Vector3f(.3f, 2.5f, .1f)), cube, shader);
        hourArm.setRenderCallback(renderCallback);
        Transform hourTransform = new Transform();
        Entity hourArmPivot = new Entity(hourTransform);
        hourArmPivot.addChild(hourArm);
        clock.addChild(hourArmPivot);

        Entity minuteArm = new ColoredEntity(new Vector3f(0, .5f, 0), new Transform(new Vector3f(0, 1, -.35f), new Vector3f(), new Vector3f(.2f, 4, .1f)), cube, shader);
        minuteArm.setRenderCallback(renderCallback);
        Transform minuteTransform = new Transform();
        Entity minuteArmPivot = new Entity(minuteTransform);
        minuteArmPivot.addChild(minuteArm);
        clock.addChild(minuteArmPivot);

        Entity secondArm = new ColoredEntity(new Vector3f(1, 0, 0), new Transform(new Vector3f(0, 1.25f, -.45f), new Vector3f(), new Vector3f(.1f, 5, .1f)), cube, shader);
        secondArm.setRenderCallback(renderCallback);
        Transform secondTransform = new Transform();
        Entity secondArmPivot = new Entity(secondTransform);
        secondArmPivot.addChild(secondArm);

        clock.addChild(secondArmPivot);
        clock.setUpdateCallback(new UpdateCallback() {
            public void invoke(Entity self) {
                Time now = Time.valueOf(LocalTime.now().plusHours(1));
                double seconds = now.getTime() / 1000.0;
                double minutes = seconds / 60;
                double hours = minutes / 60;
                hourTransform.setRotation(new Vector3f(0,0,(float) (30 * hours)));
                minuteTransform.setRotation(new Vector3f(0, 0, (float)(6 * minutes)));
                secondTransform.setRotation(new Vector3f(0, 0, (float)(6 * seconds)));
            }
        });
    }

    public void onUpdate() {
        if (Input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        clock.updateStack();
    }

    public void onRender() {
        clock.renderStack();
    }

    public void onExit() {
        clock.destroyStack();
    }

    public static void main(String... args) {
        CoffeeEngine.run(new CatLikeClock());
    }

    private static class ColoredEntity extends Entity {
        private final Vector3f color;

        ColoredEntity(Vector3f color, Component... components) {
            super(components);
            this.color = color;
        }
    }
}
