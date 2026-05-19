package br.mackenzie;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;

    /*
     * ESTADOS
     */
    private enum GameState {
        MENU,
        HOW_TO_PLAY,
        PLAYING,
        GAME_OVER
    }

    private GameState gameState = GameState.MENU;

    /*
     * MENU
     */
    private Texture menuTexture;

    private Texture comoJogarTexture;

    private boolean hasComoJogarTexture = false;

    private int menuOption = 0;

    private final String[] menuOptions = {
        "JOGAR",
        "COMO JOGAR",
        "SAIR"
    };

    private final float[] menuOptionY = {
        365,
        280,
        195
    };

    /*
     * GAME OVER
     */
    private Texture gameOverTexture;

    private boolean hasGameOverTexture = false;

    private int gameOverOption = 0;

    private final float[] gameOverOptionX = {
        375,
        650
    };

    private final float[] gameOverOptionWidth = {
        275,
        300
    };

    private final float gameOverOptionY = 225;

    /*
     * SCORE
     */
    private BitmapFont font;

    private int score = 0;

    private float scoreTimer = 0;

    /*
     * TEXTURAS JOGADOR
     */
    private Texture corredorTexture;

    private Texture agachadoTexture;

    private Texture pulandoTexture;

    /*
     * OUTRAS TEXTURAS
     */
    private Texture backgroundTexture;

    private Texture roboTexture;

    private Texture obstaculoTexture;

    private Texture destruidaTexture;

    /*
     * SPRITE ATUAL
     */
    private Texture texturaAtualJogador;

    /*
     * JOGADOR
     */
    private Rectangle corredor;

    /*
     * ROBÔ
     */
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

    /*
     * FÍSICA
     */
    private float velocityY = 0;

    private boolean pulando = false;

    private boolean deslizando = false;

    /*
     * CHÃO
     */
    private final float groundY = 100;

    /*
     * VELOCIDADE
     */
    private static final float INITIAL_GAME_SPEED = 420;

    private static final float SPEED_INCREASE_PER_LEVEL = 45;

    private static final int POINTS_PER_DIFFICULTY_LEVEL = 10;

    private float gameSpeed = INITIAL_GAME_SPEED;

    /*
     * SPAWN
     */
    private float obstacleTimer = 0;

    private final float obstacleSpawnTime = 1.3f;

    @Override
    public void create() {

        batch = new SpriteBatch();

        camera = new OrthographicCamera();

        camera.setToOrtho(false, 1280, 720);

        /*
         * MENU
         */
        menuTexture = new Texture("menu.png");

        if (Gdx.files.internal("como-jogar.png").exists()) {

            comoJogarTexture = new Texture("como-jogar.png");

            hasComoJogarTexture = true;
        }

        if (Gdx.files.internal("game-over.png").exists()) {

            gameOverTexture = new Texture("game-over.png");

            hasGameOverTexture = true;
        }

        /*
         * FONTE SCORE
         */
        font = createSharpFont(38);

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

    /*
     * FONTE
     */
    private BitmapFont createSharpFont(int size) {

        FileHandle fontFile;

        if (Gdx.files.internal("fonte.ttf").exists()) {

            fontFile = Gdx.files.internal("fonte.ttf");

        } else if (Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf").exists()) {

            fontFile = Gdx.files.absolute("C:/Windows/Fonts/arialbd.ttf");

        } else if (Gdx.files.absolute("C:/Windows/Fonts/arial.ttf").exists()) {

            fontFile = Gdx.files.absolute("C:/Windows/Fonts/arial.ttf");

        } else {

            BitmapFont fallbackFont = new BitmapFont();

            fallbackFont.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            );

            fallbackFont.getData().setScale(2.5f);

            return fallbackFont;
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;

        parameter.minFilter = Texture.TextureFilter.Linear;

        parameter.magFilter = Texture.TextureFilter.Linear;

        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "ÁÀÂÃÉÊÍÓÔÕÚÇáàâãéêíóôõúç";

        BitmapFont generatedFont = generator.generateFont(parameter);

        generatedFont.setUseIntegerPositions(false);

        generator.dispose();

        return generatedFont;
    }

    @Override
    public void render() {

        if (gameState == GameState.MENU) {

            inputMenu();

            drawMenu();

        } else if (gameState == GameState.HOW_TO_PLAY) {

            inputHowToPlay();

            drawHowToPlay();

        } else if (gameState == GameState.GAME_OVER) {

            inputGameOver();

            drawGameOver();

        } else {

            input();

            logic();

            draw();
        }
    }

    /*
     * MENU INPUT
     */
    private void inputMenu() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

            menuOption--;

            if (menuOption < 0) {

                menuOption = menuOptions.length - 1;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

            menuOption++;

            if (menuOption >= menuOptions.length) {

                menuOption = 0;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {

            if (menuOption == 0) {

                startGame();

            } else if (menuOption == 1) {

                gameState = GameState.HOW_TO_PLAY;

            } else if (menuOption == 2) {

                Gdx.app.exit();
            }
        }

        /*
         * SAIR
         */
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            Gdx.app.exit();
        }
    }

    /*
     * MENU DRAW
     */
    private void drawMenu() {

        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        /*
         * IMAGEM MENU
         */
        batch.draw(menuTexture, 0, 0, 1280, 720);

        font.draw(batch, ">", 480, menuOptionY[menuOption]);

        font.draw(batch, "<", 780, menuOptionY[menuOption]);

        batch.end();
    }

    /*
     * COMO JOGAR INPUT
     */
    private void inputHowToPlay() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            gameState = GameState.MENU;
        }
    }

    /*
     * COMO JOGAR DRAW
     */
    private void drawHowToPlay() {

        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (hasComoJogarTexture) {

            batch.draw(comoJogarTexture, 0, 0, 1280, 720);

        } else {

            font.draw(batch, "Arquivo como-jogar.png nao encontrado em assets", 220, 390);

            font.draw(batch, "Pressione ENTER ou ESC para voltar", 310, 330);
        }

        batch.end();
    }

    /*
     * GAME OVER INPUT
     */
    private void inputGameOver() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyJustPressed(Input.Keys.UP)
            || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

            gameOverOption = 1 - gameOverOption;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {

            if (gameOverOption == 0) {

                startGame();

            } else {

                resetGame();

                gameState = GameState.MENU;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            resetGame();

            gameState = GameState.MENU;
        }
    }

    /*
     * GAME OVER DRAW
     */
    private void drawGameOver() {

        Gdx.gl.glClearColor(0, 0, 0, 1);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        if (hasGameOverTexture) {

            batch.draw(gameOverTexture, 0, 0, 1280, 720);

        } else {

            font.draw(batch, "Arquivo game-over.png nao encontrado em assets", 220, 390);

            font.draw(batch, "ENTER: jogar novamente | ESC: menu", 300, 330);
        }

        font.draw(batch, ">", gameOverOptionX[gameOverOption], gameOverOptionY);

        font.draw(
            batch,
            "<",
            gameOverOptionX[gameOverOption] + gameOverOptionWidth[gameOverOption],
            gameOverOptionY
        );

        batch.end();
    }

    /*
     * INPUT GAME
     */
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

    /*
     * LÓGICA
     */
    private void logic() {

        float delta = Gdx.graphics.getDeltaTime();

        /*
         * SCORE
         */
        scoreTimer += delta;

        if (scoreTimer >= 1f) {

            score++;

            scoreTimer = 0;

            updateDifficulty();
        }

        /*
         * SPRITE
         */
        if (pulando) {

            texturaAtualJogador = pulandoTexture;

        } else if (deslizando) {

            texturaAtualJogador = agachadoTexture;

        } else {

            texturaAtualJogador = corredorTexture;
        }

        /*
         * FÍSICA
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

                obstaculo.rect.y = groundY;

                obstaculo.rect.width = MathUtils.random(45, 65);

                obstaculo.rect.height = MathUtils.random(45, 80);

            } else {

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
             * PLAYER HIT
             */
            if (!obstaculo.destruido
                && hitboxJogador.overlaps(hitboxObstaculo)) {

                gameOver();
            }

            /*
             * ROBÔ DESTROI
             */
            if (!obstaculo.destruido
                && hitboxRobo.overlaps(hitboxObstaculo)) {

                obstaculo.destruido = true;

                obstaculo.destructionTimer = 0.4f;
            }

            /*
             * TIMER
             */
            if (obstaculo.destruido) {

                obstaculo.destructionTimer -= delta;

                if (obstaculo.destructionTimer <= 0) {

                    obstaculos.removeIndex(i);

                    continue;
                }
            }

            /*
             * REMOVE
             */
            if (obstaculo.rect.x < -200) {

                obstaculos.removeIndex(i);
            }
        }

    }

    /*
     * DIFICULDADE
     */
    private void updateDifficulty() {

        int difficultyLevel = score / POINTS_PER_DIFFICULTY_LEVEL;

        gameSpeed = INITIAL_GAME_SPEED
            + (difficultyLevel * SPEED_INCREASE_PER_LEVEL);
    }

    /*
     * DRAW GAME
     */
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
         * SCORE
         */
        font.draw(batch, "SCORE: " + score, 40, 680);

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

    /*
     * START GAME
     */
    private void startGame() {

        resetGame();

        gameState = GameState.PLAYING;
    }

    /*
     * GAME OVER
     */
    private void gameOver() {

        gameOverOption = 0;

        gameState = GameState.GAME_OVER;
    }

    /*
     * RESET
     */
    private void resetGame() {

        corredor.y = groundY;

        velocityY = 0;

        pulando = false;

        deslizando = false;

        texturaAtualJogador = corredorTexture;

        gameSpeed = INITIAL_GAME_SPEED;

        score = 0;

        scoreTimer = 0;

        obstacleTimer = 0;

        obstaculos.clear();
    }

    @Override
    public void dispose() {

        batch.dispose();

        font.dispose();

        menuTexture.dispose();

        if (comoJogarTexture != null) {

            comoJogarTexture.dispose();
        }

        if (gameOverTexture != null) {

            gameOverTexture.dispose();
        }

        corredorTexture.dispose();

        agachadoTexture.dispose();

        pulandoTexture.dispose();

        backgroundTexture.dispose();

        roboTexture.dispose();

        obstaculoTexture.dispose();

        destruidaTexture.dispose();
    }
}
