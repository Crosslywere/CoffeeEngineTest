package com.test;

import com.crossly.coffee_engine.component.*;
import com.crossly.coffee_engine.component.graphics.*;
import com.crossly.coffee_engine.core.*;
import com.crossly.coffee_engine.entity.*;
import com.crossly.util.FileUtil;

import org.joml.Vector2i;
import org.joml.Vector3f;

public class TestingBasicFunctionality extends Application {

    private Camera camera;
    private Entity model;

    private Vector2i mousePosLast;

    public TestingBasicFunctionality() {
        setTitle("Testing Basic Functionality");
        setMonitorIndex(2);
        createContext();
        Framebuffer.setDepthTestEnabled(true);
    }

    @Override
    public void onCreate() {
        camera = new Camera(getWidth(), getHeight(),
                new Transform(new Vector3f(0, 2.5f, -2.5f), new Vector3f(35, 0, 0)));
        camera.setUpdateCallback(self -> {
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
                        transform.addPosition(
                            transform.getFront().mul(movement.z()).add(
                                transform.getRight().mul(movement.x())).add(
                                    transform.getUp().mul(movement.y()
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
                        transform.addRotation(new Vector3f(yoffset, xoffset, 0).mul(Timer.getDeltaTime() * 2f));
                        transform.setPitch(Math.clamp(transform.getPitch(), -89, 89));
                        mousePosLast = mousePos;
                        Input.disableMouse();
                    }
                    if (!Input.isButtonPressed(Input.MOUSE_BUTTON_LEFT)) {
                        Input.normalMouse();
                    }
                });
            }
        );
        model = Entity.create(new GraphicsShader(FileUtil.getFileString("shaders/shader.vert"),
                FileUtil.getFileString("shaders/shader.frag")), new StaticMesh("models/Queen.obj"));
        model.setUpdateCallback((self) -> {
            self.getComponent(Transform.class).ifPresent(transform -> {
                transform.addYaw((float) Math.toDegrees(Timer.getDeltaTime()));
            });
        });
        model.setRenderCallback(self -> {
            self.getComponent(GraphicsShader.class).ifPresent(shader -> {
                shader.setMatrix4f("uProjection", camera.getProjection3D());
                shader.setMatrix4f("uView", camera.getView());
                shader.setMatrix4f("uModel", self.getComponent(Transform.class).get().getModelMatrix());
                shader.setFloat3("uColor", new Vector3f(1));
                self.getComponent(StaticMesh.class).ifPresent(Mesh::render);
            });
        });
    }

    @Override
    public void onUpdate() {
        if (Input.isKeyPressed(Input.KEY_ESCAPE)) quit();
        if (Input.isKeyJustPressed(Input.KEY_F)) setFullscreen(!isFullscreen());
        camera.update();
        model.update();
    }

    @Override
    public void onRender() {
        model.render();
    }

    @Override
    public void onExit() {
        model.destroyStack();
    }

    public static void main(String... args) {
        CoffeeEngine.run(new TestingBasicFunctionality());
    }
}
