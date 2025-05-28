package com.test;

import com.crossly.CoffeeEngine;
import com.crossly.components.Mesh;
import com.crossly.components.ShaderProgram;
import com.crossly.entities.Camera3D;
import com.crossly.entities.Entity;
import com.crossly.input.Input;
import com.crossly.interfaces.Application;
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
        ShaderProgram faceShader = ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build();
        faceShader.use();
        faceShader.setFloat3("color", new Vector3f(1));
        Mesh.Buffer faceMesh = Mesh.Buffer.create("models/Cylinder.obj");
        // Clock face creation
        Entity clockFace = new Entity(faceMesh, faceShader);
        clockFace.getTransform().reset();
        clockFace.getTransform().setScale(new Vector3f(10, .2f, 10));
        clockFace.getTransform().setPitch((float) Math.toRadians(90));

        clock.addChild(clockFace);
        // Hour indicator components creation
        ShaderProgram hourIndicatorShader = ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build();
        hourIndicatorShader.use();
        hourIndicatorShader.setFloat3("color", new Vector3f(73f / 255));
        Mesh.Buffer hourIndicatorMesh = Mesh.Buffer.create("models/Cube.obj");

        // Clock hour indicators
        for (int i = 0; i < 12; i++) {
            Entity hourIndicator = new Entity(hourIndicatorShader, hourIndicatorMesh);
            hourIndicator.getTransform().setScale(new Vector3f(.5f, 1, .1f));
            switch (i) {
                case 0 -> hourIndicator.getTransform().setPosition(new Vector3f(0, 4, -.25f));
                case 1 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(-2, 3.464f, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(30));
                }
                case 2 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(-3.464f, 2, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(60));
                }
                case 3 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(-4, 0, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(90));
                }
                case 4 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(-3.464f, -2, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(120));
                }
                case 5 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(-2, -3.464f, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(150));
                }
                case 6 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(0, -4, -.25f));
                    hourIndicator.getTransform().setRoll((float) Math.toRadians(180));
                }
                case 7 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(2, -3.464f, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(210));
                }
                case 8 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(3.464f, -2, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(240));
                }
                case 9 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(4, 0, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(270));
                }
                case 10 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(3.464f, 2, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(300));
                }
                case 11 -> {
                    hourIndicator.getTransform().setPosition(new Vector3f(2, 3.464f, -.25f));
                    hourIndicator.getTransform().setRoll((float)Math.toRadians(330));
                }
            }
            clock.addChild(hourIndicator);
        }

        // Clock hands
        Entity hourArm = new Entity(hourIndicatorMesh, ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build());
        hourArm.getTransform().setPosition(new Vector3f(0, -.25f, -.25f));
        hourArm.getTransform().setScale(new Vector3f(.3f, 2.5f, .1f));
        hourArmPivot = new Entity();
        hourArmPivot.addChild(hourArm);
        Entity minuteArm = new Entity(hourIndicatorMesh, ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build());
        minuteArm.getTransform().setPosition(new Vector3f(0, -.5f, -.35f));
        minuteArm.getTransform().setScale(new Vector3f(.2f, 4, .1f));
        minuteArmPivot = new Entity();
        minuteArmPivot.addChild(minuteArm);
        Entity secondArm = new Entity(hourIndicatorMesh, ShaderProgram.builder()
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/color_shader.frag")
                .setView("uView")
                .setProjection("uProjection")
                .setModel("uModel")
                .build());
        secondArm.getShaderComponent().ifPresent(
                shader-> {
                    shader.use();
                    shader.setFloat3("color", new Vector3f(1, 0, 0));
                }
        );
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

    public static void main(String... args) {
        CoffeeEngine.run(CatLikeClock.class);
    }
}
