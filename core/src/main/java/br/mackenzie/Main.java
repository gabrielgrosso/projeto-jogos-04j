package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;

    // Texturas
    private Texture corredorTexture;
    private Texture backgroundTexture;
    private Texture roboTexture;
    private Texture obstaculoTexture;

    // Personagem
    private Rectangle corredor;

    // Robô perseguidor
    private Rectangle robo;

    // Obstáculos
    private Array<Rectangle> obstaculos;

    // Sistema de lanes
    private final int LEFT_LANE = 180;
    private final int CENTER_LANE = 400;
    private final int RIGHT_LANE = 620;

    private int currentLane = CENTER_LANE;

    // Física
    private float velocityY = 0;
    private boolean pulando = false;
    private boolean deslizando = false;

    // Controle de obstáculos
    private float obstacleTimer = 0;
    private final float obstacleSpawnTime = 1.2f;

    // Velocidade do jogo
    private float gameSpeed = 500;

    // Chão
    private final float groundY = 80;

    @Override
    public void create() {

        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        // Carregando texturas
        corredorTexture = new Texture("corredor.png");
        backgroundTexture = new Texture("background.png");
        roboTexture = new Texture("robo.png");
        obstaculoTexture = new Texture("obstaculo.png");

        // Criando corredor
        corredor = new Rectangle();
        corredor.x = CENTER_LANE;
        corredor.y = groundY;
        corredor.width = 80;
        corredor.height = 120;

        // Criando robô
        robo = new Rectangle();
        robo.x = CENTER_LANE;
        robo.y = groundY;
        robo.width = 80;
        robo.height = 120;

        obstaculos = new Array<>();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

        input();
        logic();
        draw();
    }

    private void input() {

        // Direita
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {

            if (currentLane == LEFT_LANE) {
                currentLane = CENTER_LANE;
            } else if (currentLane == CENTER_LANE) {
                currentLane = RIGHT_LANE;
            }
        }

        // Esquerda
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {

            if (currentLane == RIGHT_LANE) {
                currentLane = CENTER_LANE;
            } else if (currentLane == CENTER_LANE) {
                currentLane = LEFT_LANE;
            }
        }

        // Pulo
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && !pulando) {
            velocityY = 900;
            pulando = true;
        }

        // Deslizar
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && !pulando) {
            deslizando = true;
            corredor.height = 70;
        } else {
            deslizando = false;
            corredor.height = 120;
        }
    }

    private void logic() {

        float delta = Gdx.graphics.getDeltaTime();

        // Movimento lateral suave
        corredor.x += (currentLane - corredor.x) * 10 * delta;

        // Gravidade
        velocityY -= 1800 * delta;
        corredor.y += velocityY * delta;

        // Limite do chão
        if (corredor.y <= groundY) {
            corredor.y = groundY;
            velocityY = 0;
            pulando = false;
        }

        // Robô acompanha lentamente
        robo.x += (corredor.x - robo.x) * 2f * delta;

        // Spawn de obstáculos
        obstacleTimer += delta;

        if (obstacleTimer >= obstacleSpawnTime) {

            Rectangle obstaculo = new Rectangle();

            int laneRandom = MathUtils.random(0, 2);

            if (laneRandom == 0) {
                obstaculo.x = LEFT_LANE;
            } else if (laneRandom == 1) {
                obstaculo.x = CENTER_LANE;
            } else {
                obstaculo.x = RIGHT_LANE;
            }

            obstaculo.y = 480;
            obstaculo.width = 70;
            obstaculo.height = 70;

            obstaculos.add(obstaculo);

            obstacleTimer = 0;
        }

        // Movimento obstáculos
        for (int i = obstaculos.size - 1; i >= 0; i--) {

            Rectangle obstaculo = obstaculos.get(i);

            obstaculo.y -= gameSpeed * delta;

            // Colisão
            if (obstaculo.overlaps(corredor)) {

                System.out.println("GAME OVER");

                // Reinicia posição
                corredor.x = CENTER_LANE;
                corredor.y = groundY;

                obstaculos.clear();
            }

            // Remove obstáculos fora da tela
            if (obstaculo.y < -100) {
                obstaculos.removeIndex(i);
            }
        }

        // Aumenta dificuldade gradualmente
        gameSpeed += 2 * delta;
    }

    private void draw() {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Background
        batch.draw(backgroundTexture, 0, 0, 800, 480);

        // Robô
        batch.draw(
            roboTexture,
            robo.x,
            robo.y,
            robo.width,
            robo.height
        );

        // Corredor
        batch.draw(
            corredorTexture,
            corredor.x,
            corredor.y,
            corredor.width,
            corredor.height
        );

        // Obstáculos
        for (Rectangle obstaculo : obstaculos) {

            batch.draw(
                obstaculoTexture,
                obstaculo.x,
                obstaculo.y,
                obstaculo.width,
                obstaculo.height
            );
        }

        batch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

        batch.dispose();

        corredorTexture.dispose();
        backgroundTexture.dispose();
        roboTexture.dispose();
        obstaculoTexture.dispose();
    }
}
