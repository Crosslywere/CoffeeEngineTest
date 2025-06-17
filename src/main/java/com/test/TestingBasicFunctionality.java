package com.test;

import com.crossly.coffee_engine.component.*;
import com.crossly.coffee_engine.core.*;
import com.crossly.coffee_engine.entity.*;
import com.crossly.util.FileUtil;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class TestingBasicFunctionality extends Application {

    private Camera camera;
    private Entity model;

    private Vector2i mousePosLast = new Vector2i();

    public TestingBasicFunctionality() {
        setTitle("Testing Basic Functionality");
        setMonitorIndex(2);
        createContext();
        Framebuffer.setDepthTest(true);
    }

    @Override
    public void onCreate() {
        camera = new Camera(getWidth(), getHeight(), new Shader(
                """
                                #version 330 core
                                layout (location = 0) in vec3 aPos;
                                void main() {
                                gl_Position = vec4(aPos, 1.0);
                                }""",
                """
                                #version 330 core
                                layout (location = 0) out vec4 oColor;
                                uniform sampler2D render;
                                uniform vec2 resolution;
                                #define PIXEL_SIZE 2.0
                                void main() {
                                vec2 uv = floor(gl_FragCoord.xy / PIXEL_SIZE) * PIXEL_SIZE;
                                uv /= resolution;
                                oColor = texture2D(render, uv);
                                }"""), Framebuffer.builder().add2DColor().add2DDepth());
        camera.getComponent(Transform.class).ifPresent(
                transform -> {
                    transform.setPosition(new Vector3f(0, 2.5f, -2.5f));
                    transform.setRotation(new Vector3f(35, 0, 0));
                }
        );
        camera.setUpdateCallback(new UpdateCallback() {
            @Override
            public void invoke(Entity self) {
                self.getComponent(Transform.class).ifPresent(
                        transform -> {
                            Vector3f movement = new Vector3f();
                            if (Input.isKeyPressed(Input.KEY_W))
                                movement.z += 1;
                            if (Input.isKeyPressed(Input.KEY_S))
                                movement.z -= 1;
                            if (Input.isKeyPressed(Input.KEY_D))
                                movement.x += 1;
                            if (Input.isKeyPressed(Input.KEY_A))
                                movement.x -= 1;
                            if (Input.isKeyPressed(Input.KEY_SPACE))
                                movement.y += 1;
                            if (Input.isKeyPressed(Input.KEY_LEFT_CTRL))
                                movement.y -= 1;
                            if (movement.length() != 0) {
                                movement.normalize().mul(Timer.getDeltaTime());
                                transform.setPosition(
                                        transform.getPosition().add(
                                                transform.getFront().mul(movement.z()).add(
                                                        transform.getRight().mul(movement.x()).add(
                                                                transform.getUp().mul(movement.y())
                                                        )
                                                )
                                        )
                                );
                            }
                            // Camera rotation
                            if (Input.isButtonPressed(Input.MOUSE_BUTTON_LEFT)) {
                                var mousePos = Input.getMousePos();
                                if (Input.isButtonJustPressed(Input.MOUSE_BUTTON_LEFT)) {
                                    mousePosLast = Input.getMousePos();
                                }
                                float xoffset = mousePosLast.x() - mousePos.x();
                                float yoffset = mousePos.y() - mousePosLast.y();
                                transform.setRotation(transform.getRotation().add(new Vector3f(yoffset, xoffset, 0)).mul(Timer.getDeltaTime() * 2));
                                transform.setPitch(Math.clamp(transform.getPitch(), -89, 89));
                                mousePosLast = mousePos;
                                Input.disableMouse();
                            }
                            if (!Input.isButtonPressed(Input.MOUSE_BUTTON_LEFT)) {
                                Input.normalMouse();
                            }
                        }
                );
            }
        });
        camera.setRenderCallback(new RenderCallback() {
            @Override
            public void invoke(Entity self) {
                Framebuffer.unbind();
                var shader = self.getComponent(Shader.class).orElse(null);
                var framebuffer = self.getComponent(Framebuffer.class).orElse(null);
                if (shader == null || framebuffer == null) return;
                shader.use();
                shader.setFloat2("resolution", new Vector2f(getWidth(), getHeight()));
                shader.setTexture("render", framebuffer.getTexture(0), 0);
                StaticMesh.UNIT_RECT.render();
            }
        });
        model = Entity.create(new Shader(FileUtil.getFileString("shaders/shader.vert"), FileUtil.getFileString("shaders/shader.frag")), new StaticMesh("models/Queen.obj"));
        model.setRenderCallback(new RenderCallback() {
            @Override
            public void invoke(Entity self) {
                self.getComponent(Shader.class).ifPresent((shader) -> {
                    shader.setMatrix4f("uProjection", camera.getProjection3D());
                    shader.setMatrix4f("uView", camera.getView());
                    shader.setMatrix4f("uModel", self.getComponent(Transform.class).get().getModelMatrix());
                    shader.setFloat3("uColor", new Vector3f(1));
                    self.getComponent(StaticMesh.class).ifPresent(Mesh::render);
                });
            }
        });
    }

    @Override
    public void onUpdate() {
        if (Input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        if (Input.isKeyJustPressed(Input.KEY_F)) setFullscreen(!isFullscreen());
        camera.update();
    }

    @Override
    public void onRender() {
        camera.drawToFramebuffer(model);
        camera.render();
    }

    @Override
    public void onExit() {
        model.destroyStack();
    }

    public static void main(String... args) {
        CoffeeEngine.run(new TestingBasicFunctionality());
    }
}
