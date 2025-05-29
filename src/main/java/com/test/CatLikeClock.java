package com.test;

import com.crossly.CoffeeEngine;
import com.crossly.components.Model;
import com.crossly.components.ShaderProgram;
import com.crossly.entities.Camera3D;
import com.crossly.entities.Entity;
import com.crossly.input.Input;
import com.crossly.interfaces.Application;
import com.crossly.interfaces.Component;
import org.joml.Vector3f;

import java.sql.Time;
import java.time.LocalTime;

import static org.lwjgl.opengl.GL11C.glClearColor;

public class CatLikeClock extends Application {

    private final Camera3D camera;
    private final Entity clock, hourArmPivot, minuteArmPivot, secondArmPivot;

    public CatLikeClock() {
        super(1280, 720, "CatLikeCoding Clock");
        camera = new Camera3D(getWindowWidth(), getWindowHeight());
        camera.getTransform().reset();
        camera.getTransform().setPosition(new Vector3f(0, 0, -15));
        clock = new Entity();
        // Clock face components creation
        ShaderProgram shader = ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build();
        var cylinder = new Model("models/Cylinder.obj");
        // Clock face creation
        Entity clockFace = new ColoredEntity(new Vector3f(1), cylinder, shader);
        clockFace.getTransform().reset();
        clockFace.getTransform().setScale(new Vector3f(10, .2f, 10));
        clockFace.getTransform().setPitch((float) Math.toRadians(90));

        clock.addChild(clockFace);
        // Hour indicator components creation
        Model cube = new Model("models/Cube.obj");

        Vector3f[] positions = {
                new Vector3f(0, 4, -.25f),// 12
                new Vector3f(-2, 3.464f, -.25f),// 1
                new Vector3f(-3.464f, 2, -.25f),// 2
                new Vector3f(-4, 0, -.25f),// 3
                new Vector3f(-3.464f, -2, -.25f),// 4
                new Vector3f(-2, -3.464f, -.25f),// 5
                new Vector3f(0, -4, -.25f),// 6
                new Vector3f(2, -3.464f, -.25f),// 7
                new Vector3f(3.464f, -2, -.25f),// 8
                new Vector3f(4, 0, -.25f),// 9
                new Vector3f(3.464f, 2, -.25f),// 10
                new Vector3f(2, 3.464f, -.25f),// 11
        };
        int rot = 0;
        // Clock hour indicators
        for (Vector3f position : positions) {
            Entity hourIndicator = new ColoredEntity(new Vector3f(73f / 255), cube, shader);
            hourIndicator.getTransform().setPosition(position);
            hourIndicator.getTransform().setRoll((float) Math.toRadians(rot));
            hourIndicator.getTransform().setScale(new Vector3f(.5f, 1, .1f));
            clock.addChild(hourIndicator);
            rot += 30;
        }

        // Clock hands
        Entity hourArm = new ColoredEntity(new Vector3f(), cube, shader);
        hourArm.getTransform().setPosition(new Vector3f(0, -.25f, -.25f));
        hourArm.getTransform().setScale(new Vector3f(.3f, 2.5f, .1f));
        hourArmPivot = new Entity();
        hourArmPivot.addChild(hourArm);
        Entity minuteArm = new ColoredEntity(new Vector3f(), cube, shader);
        minuteArm.getTransform().setPosition(new Vector3f(0, -.5f, -.35f));
        minuteArm.getTransform().setScale(new Vector3f(.2f, 4, .1f));
        minuteArmPivot = new Entity();
        minuteArmPivot.addChild(minuteArm);
        Entity secondArm = new ColoredEntity(new Vector3f(1, 0, 0),cube, shader);
        secondArm.getTransform().setPosition(new Vector3f(0, -.75f, -.45f));
        secondArm.getTransform().setScale(new Vector3f(.1f, 5, .1f));
        secondArmPivot = new Entity();
        secondArmPivot.addChild(secondArm);
        clock.addChildren(hourArmPivot, minuteArmPivot, secondArmPivot);
        glClearColor(0, 0.5f, 1, 1);
    }

    @Override
    public void onUpdate(Input input) {
        if (input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        Time now = Time.valueOf(LocalTime.now());
        double seconds = now.getTime() / 1000.0;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        hourArmPivot.getTransform().setRoll((float) Math.toRadians(30 * hours));
        minuteArmPivot.getTransform().setRoll((float) Math.toRadians(6 * minutes));
        secondArmPivot.getTransform().setRoll((float) Math.toRadians(6 * seconds));
    }

    @Override
    public void onRender() {
        camera.makeActive();
        clock.render();
        camera.render();
    }

    @Override
    public void onExit() {
        clock.cleanup();
    }

    public static void main(String... args) {
        CoffeeEngine.run(CatLikeClock.class);
    }

    private static class ColoredEntity extends Entity {
        private final Vector3f color;

        public ColoredEntity(Vector3f color, Component... components) {
            super(components);
            this.color = color;
        }

        @Override
        public void render() {
            this.getShaderComponent().ifPresent((shader) -> {
                shader.use();
                CoffeeEngine.getCurrentActiveCamera().ifPresent((camera) -> {
                    shader.setProjectionMatrix(camera.getProjection());
                    shader.setViewMatrix(camera.getView());
                });
                shader.setModelMatrix(this.transform.getTransformMatrix());
                shader.setFloat3("color", color);
                this.components.values().forEach(Component::render);
            });
            this.children.forEach(Entity::render);
        }
    }
}
