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

    // Texturas jogador
    private Texture corredorTexture;
    private Texture agachadoTexture;
    private Texture pulandoTexture;

    // Outras texturas
    private Texture backgroundTexture;
    private Texture roboTexture;
    private Texture obstaculoTexture;

    // Caixa destruída
    private Texture destruidaTexture;

    // Sprite atual jogador
    private Texture texturaAtualJogador;

    // Jogador
    private Rectangle corredor;

    // Robô
    private Rectangle robo;

    /*
     * CLASSE OBSTÁCULO
     */
    private static class Obstaculo {

        Rectangle rect;

        boolean destruido = false;

        float destructionTimer = 0;

        int tipo;
    }

    private Array<Obstaculo> obstaculos;

    // Física
    private float velocityY = 0;

    private boolean pulando = false;

    private boolean deslizando = false;

    // Chão
    private final float groundY = 100;

    // Velocidade
    private float gameSpeed = 500;

    // Spawn
    private float obstacleTimer = 0;

    private final float obstacleSpawnTime = 1.3f;

    @Override
    public void create() {

        batch = new SpriteBatch();

        camera = new OrthographicCamera();

        camera.setToOrtho(false, 1280, 720);

        /*
         * TEXTURAS
         */
        corredorTexture = new Texture("corredor.png");

        agachadoTexture = new Texture("agachado.png");

        pulandoTexture = new Texture("pulando.png");

        backgroundTexture = new Texture("background.png");

        roboTexture = new Texture("robo.png");

        obstaculoTexture = new Texture("obstaculo.png");

        destruidaTexture = new Texture("destruida.png");

        texturaAtualJogador = corredorTexture;

        /*
         * JOGADOR
         */
        corredor = new Rectangle();

        corredor.x = 250;

        corredor.y = groundY;

        corredor.width = 80;

        corredor.height = 110;

        /*
         * ROBÔ
         */
        robo = new Rectangle();

        robo.x = 120;

        robo.y = groundY;

        robo.width = 75;

        robo.height = 105;

        obstaculos = new Array<>();
    }

    @Override
    public void render() {

        input();

        logic();

        draw();
    }

    private void input() {

        /*
         * PULO
         */
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)
            && !pulando
            && !deslizando) {

            velocityY = 950;

            pulando = true;
        }

        /*
         * AGACHAR
         */
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)
            && !pulando) {

            deslizando = true;

            corredor.height = 60;

        } else {

            deslizando = false;

            corredor.height = 110;
        }
    }

    private void logic() {

        float delta = Gdx.graphics.getDeltaTime();

        /*
         * SPRITE JOGADOR
         */
        if (pulando) {

            texturaAtualJogador = pulandoTexture;

        } else if (deslizando) {

            texturaAtualJogador = agachadoTexture;

        } else {

            texturaAtualJogador = corredorTexture;
        }

        /*
         * FÍSICA PULO
         */
        velocityY -= 2200 * delta;

        corredor.y += velocityY * delta;

        /*
         * CHÃO
         */
        if (corredor.y <= groundY) {

            corredor.y = groundY;

            velocityY = 0;

            pulando = false;
        }

        /*
         * ROBÔ
         */
        robo.y = groundY;

        /*
         * SPAWN
         */
        obstacleTimer += delta;

        if (obstacleTimer >= obstacleSpawnTime) {

            Obstaculo obstaculo = new Obstaculo();

            obstaculo.rect = new Rectangle();

            obstaculo.rect.x = 1400;

            int tipo = MathUtils.random(0, 1);

            obstaculo.tipo = tipo;

            if (tipo == 0) {

                // Chão
                obstaculo.rect.y = groundY;

                obstaculo.rect.width = MathUtils.random(45, 65);

                obstaculo.rect.height = MathUtils.random(45, 80);

            } else {

                // Alto
                obstaculo.rect.y = groundY + 70;

                obstaculo.rect.width = MathUtils.random(70, 110);

                obstaculo.rect.height = 35;
            }

            obstaculos.add(obstaculo);

            obstacleTimer = 0;
        }

        /*
         * MOVIMENTAÇÃO
         */
        for (int i = obstaculos.size - 1; i >= 0; i--) {

            Obstaculo obstaculo = obstaculos.get(i);

            obstaculo.rect.x -= gameSpeed * delta;

            /*
             * HITBOXES
             */
            Rectangle hitboxJogador = new Rectangle(
                corredor.x + 15,
                corredor.y + 10,
                corredor.width - 30,
                corredor.height - 10
            );

            Rectangle hitboxObstaculo = new Rectangle(
                obstaculo.rect.x + 5,
                obstaculo.rect.y + 5,
                obstaculo.rect.width - 10,
                obstaculo.rect.height - 10
            );

            Rectangle hitboxRobo = new Rectangle(
                robo.x + 10,
                robo.y + 10,
                robo.width - 20,
                robo.height - 10
            );

            /*
             * COLISÃO JOGADOR
             */
            if (!obstaculo.destruido
                && hitboxJogador.overlaps(hitboxObstaculo)) {

                System.out.println("GAME OVER");

                resetGame();
            }

            /*
             * ROBÔ DESTRÓI
             */
            if (!obstaculo.destruido
                && hitboxRobo.overlaps(hitboxObstaculo)) {

                obstaculo.destruido = true;

                obstaculo.destructionTimer = 0.4f;
            }

            /*
             * TIMER DESTRUIÇÃO
             */
            if (obstaculo.destruido) {

                obstaculo.destructionTimer -= delta;

                if (obstaculo.destructionTimer <= 0) {

                    obstaculos.removeIndex(i);

                    continue;
                }
            }

            /*
             * REMOVE FORA TELA
             */
            if (obstaculo.rect.x < -200) {

                obstaculos.removeIndex(i);
            }
        }

        /*
         * DIFICULDADE
         */
        gameSpeed += 3 * delta;
    }

    private void draw() {

        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        /*
         * BACKGROUND
         */
        batch.draw(backgroundTexture, 0, 0, 1280, 720);

        /*
         * ROBÔ
         */
        batch.draw(
            roboTexture,
            robo.x,
            robo.y,
            robo.width,
            robo.height
        );

        /*
         * JOGADOR
         */
        batch.draw(
            texturaAtualJogador,
            corredor.x,
            corredor.y,
            corredor.width,
            corredor.height
        );

        /*
         * OBSTÁCULOS
         */
        for (Obstaculo obstaculo : obstaculos) {

            Texture textura;

            if (obstaculo.destruido) {

                textura = destruidaTexture;

            } else {

                textura = obstaculoTexture;
            }

            batch.draw(
                textura,
                obstaculo.rect.x,
                obstaculo.rect.y,
                obstaculo.rect.width,
                obstaculo.rect.height
            );
        }

        batch.end();
    }

    private void resetGame() {

        corredor.y = groundY;

        velocityY = 0;

        pulando = false;

        deslizando = false;

        texturaAtualJogador = corredorTexture;

        gameSpeed = 500;

        obstaculos.clear();
    }

    @Override
    public void dispose() {

        batch.dispose();

        corredorTexture.dispose();

        agachadoTexture.dispose();

        pulandoTexture.dispose();

        backgroundTexture.dispose();

        roboTexture.dispose();

        obstaculoTexture.dispose();

        destruidaTexture.dispose();
    }
}
