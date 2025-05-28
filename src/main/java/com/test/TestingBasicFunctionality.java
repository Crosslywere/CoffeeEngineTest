package com.test;

import com.crossly.CoffeeEngine;
import com.crossly.components.Mesh;
import com.crossly.components.ShaderProgram;
import com.crossly.components.subcomponents.Transform;
import com.crossly.entities.Camera3D;
import com.crossly.entities.Entity;
import com.crossly.input.Input;
import com.crossly.interfaces.Application;
import com.crossly.timer.Timer;
import org.joml.Vector2i;

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
        camera = new Camera3D(getWindowWidth(), getWindowHeight());
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
                                #define PIXEL_SIZE 2.0
                                void main() {
                                    vec2 uv = floor(gl_FragCoord.xy / PIXEL_SIZE) * PIXEL_SIZE;
                                    uv /= resolution;
                                    oColor = texture2D(render, uv);
                                }""")
                .build(), "render");
        camera.getTransform().setPositionY(2.5f);
        camera.getTransform().setPositionZ(-2.5f);
        camera.getTransform().setPitch((float) Math.toRadians(35));
    }

    @Override
    public void onUpdate(Input input) {
        if (input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        if (input.isKeyJustPressed(Input.KEY_F)) setFullscreen(!getFullscreen());
        // Moving the camera based on
        if (input.isKeyPressed(Input.KEY_W))
            camera.getTransform().incrementPosition(camera.getTransform().getFront().mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_S))
            camera.getTransform().incrementPosition(camera.getTransform().getFront().mul(-Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_A))
            camera.getTransform().incrementPosition(camera.getTransform().getRight().mul(-Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_D))
            camera.getTransform().incrementPosition(camera.getTransform().getRight().mul(Timer.getDeltaTime()));

        if (input.isKeyPressed(Input.KEY_SPACE))
            camera.getTransform().incrementPosition(Transform.getWorldUp().mul(Timer.getDeltaTime()));
        if (input.isKeyPressed(Input.KEY_LEFT_CTRL))
            camera.getTransform().incrementPosition(Transform.getWorldUp().mul(-Timer.getDeltaTime()));

        // Orienting the camera's roll
        if (input.isScrollDown())
            camera.getTransform().setRoll(camera.getTransform().getRoll() + Timer.getDeltaTime());
        if (input.isScrollUp())
            camera.getTransform().setRoll(camera.getTransform().getRoll() - Timer.getDeltaTime());

        // Orienting the camera's pitch and yaw
        if (input.isButtonPressed(Input.MOUSE_BUTTON_LEFT)) {
            var mousePos = input.getMousePos();
            if (mouseFirst) {
                mousePosLast = input.getMousePos();
                mouseFirst = false;
            }
            float xoffset = mousePosLast.x() - mousePos.x();
            float yoffset = mousePos.y() - mousePosLast.y();
            camera.getTransform().setPitch(Math.clamp(camera.getTransform().getPitch() + (yoffset * Timer.getDeltaTime() * .05f), -CLAMP_CAM, CLAMP_CAM));
            camera.getTransform().setYaw(camera.getTransform().getYaw() + (xoffset * Timer.getDeltaTime() * .05f));
            mousePosLast = input.getMousePos();
            input.disableMouse();
        }
        else {
            mouseFirst = true;
            input.normalMouse();
        }
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
