package com.test;

import com.crossly.CoffeeEngine;
import com.crossly.components.Mesh;
import com.crossly.components.ShaderProgram;
import com.crossly.entities.Camera3D;
import com.crossly.entities.Entity;
import com.crossly.input.Input;
import com.crossly.interfaces.Application;
import com.crossly.timer.Timer;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class TestingBasicFunctionality extends Application {

    private final Entity entity;
    private final Camera3D camera;
    private boolean mouseFirst = true;
    private Vector2i mousePosLast = new Vector2i();

    private final float CLAMP_CAM = (float) Math.toRadians(89.9);

    public TestingBasicFunctionality() {
        super(800, 600, "Testing Basic Functionality");
        ShaderProgram shader = ShaderProgram.builder()
                .setProjection("uProjection")
                .setView("uView")
                .setModel("uModel")
                .attachVertexShaderFile("shaders/shader.vert")
                .attachFragmentShaderFile("shaders/shader.frag")
                .build();
        shader.use();
        shader.setFloat3("uColor", 1, 1, 1);
        Mesh.Buffer mesh = Mesh.Buffer.create("models/Queen.obj");
        entity = new Entity(shader, mesh);
        entity.getTransform().setPosition(new Vector3f(0, 1, 0));
        camera = new Camera3D(getWindowWidth(), getWindowHeight());
        camera.getTransform().reset();
        camera.setScreenShader(ShaderProgram.builder()
                        .attachVertexShader("""
                                #version 330 core
                                layout (location = 0) in vec3 aPos;
                                void main() {
                                    gl_Position = vec4(aPos, 1.0);
                                }""")
                        .attachFragmentShader("""
                                #version 330 core
                                layout (location = 0) out vec4 oColor;
                                uniform sampler2D render;
                                uniform vec2 resolution;
                                #define PIXEL_SIZE 1.0
                                void main() {
                                    vec2 uv = floor(gl_FragCoord.xy / PIXEL_SIZE) * PIXEL_SIZE;
                                    uv /= resolution;
                                    oColor = texture2D(render, uv);
                                }""")
                .build(), "render");
    }

    @Override
    public void onUpdate(Input input) {
        if (input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        if (input.isKeyJustPressed(Input.KEY_F)) setFullscreen(!getFullscreen());
        if (input.isKeyPressed(Input.KEY_W))
            camera.getTransform().incrementPosition(camera.getTransform().getForward().mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_S))
            camera.getTransform().incrementPosition(camera.getTransform().getForward().mul(-Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_A))
            camera.getTransform().incrementPosition(camera.getTransform().getRight().mul(-Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_D))
            camera.getTransform().incrementPosition(camera.getTransform().getRight().mul(Timer.getDeltaTime()));

        if (input.isKeyPressed(Input.KEY_SPACE))
            camera.getTransform().incrementPosition(new Vector3f(0, 1, 0).mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_LEFT_CTRL))
            camera.getTransform().incrementPosition(new Vector3f(0,-1, 0).mul(Timer.getDeltaTime()));

        if (input.isKeyPressed(Input.KEY_I))
            camera.getTransform().incrementRotation(new Vector3f(1, 0, 0).mul(-Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_K))
            camera.getTransform().incrementRotation(new Vector3f(1, 0, 0).mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_J))
            camera.getTransform().incrementRotation(new Vector3f(0, 1, 0).mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_L))
            camera.getTransform().incrementRotation(new Vector3f(0, 1, 0).mul(-Timer.getDeltaTime()));

        if (input.isButtonPressed(Input.MOUSE_BUTTON_LEFT)) {
            var mousePos = input.getMousePos();
            if (mouseFirst) {
                mousePosLast = input.getMousePos();
                mouseFirst = false;
            }
            float xoffset = mousePos.x() - mousePosLast.x();
            float yoffset = mousePosLast.y() - mousePos.y();
            camera.getTransform().incrementRotation(new Vector3f(yoffset, xoffset, 0).mul(Timer.getDeltaTime() * .05f));
            mousePosLast = input.getMousePos();
            input.disableMouse();
        }
        else {
            mouseFirst = true;
            input.normalMouse();
        }

        if (input.isKeyJustPressed(Input.KEY_T)) {
            System.out.println(camera.getTransform().getForward());
        }
        camera.getTransform().getRotation().x = Math.clamp(camera.getTransform().getRotation().x(), -CLAMP_CAM, CLAMP_CAM);
    }

    @Override
    public void onRender() {
        camera.makeActive();
        camera.getScreenShader().ifPresent(
                shader -> shader.setFloat2("resolution", getWindowWidth(), getWindowHeight())
        );
        entity.render();
        camera.render();
    }

    @Override
    public void onExit() {
        entity.cleanup();
        camera.cleanup();
    }

    public static void main(String[] args) {
        CoffeeEngine.run(TestingBasicFunctionality.class);
    }
}
