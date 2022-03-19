import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Snake extends Application {
  static boolean gameOver = false;
  static int width = 80;
  static int height = 50;
  static int box_Size = 20;
  static int scaled_width = width * box_Size;
  static int scaled_height = height * box_Size;
  static ArrayList<Box> snake = new ArrayList<Box>();
  static Dir direction = Dir.up;
  static Random rand = new Random();
  static boolean Paused = true;
  static int high_score = 0;
  static int current_score = snake.size() - 3;

  // User Settings
  static int fps = 60;
  static int fruit_amount = 10;
  static boolean disable_bad_fruits = false;
  static Color border = Color.LIGHTGRAY;
  static Color game_background = Color.BEIGE;
  static Color game_background1 = Color.BISQUE;
  static Color snake_color = Color.MEDIUMSEAGREEN;
  static Color snake_head = Color.GREEN;
  static Color good_fruit = Color.TOMATO;
  static Color bad_fruit = Color.MEDIUMAQUAMARINE;

  // An enum listing all possible directions for the snake to travel in. Relative
  // to player's screen.
  public enum Dir {
    left, right, up, down
  }

  // CLASSES
  public static class Box {
    int x;
    int y;

    public Box(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public abstract static class Fruit {
    protected int x;
    protected int y;

    Fruit(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public static class good_fruit extends Fruit {
    good_fruit(int x, int y) {
      super(x, y);
    }
  }

  public static class bad_fruit extends Fruit {
    bad_fruit(int x, int y) {
      super(x, y);
    }
  }

  public static class MyTimer extends AnimationTimer {
    long last = 0;
    long interval = 1000000000L / fps;

    @Override
    public void handle(long now) {
      if (last == 0) {
        last = now;
        tick(gc);
        return;
      }
      if (now - last > interval) {
        last = now;
        if (!Paused) {
          tick(gc);
          return;
        } else {
          gc.setFill(Color.BLACK);
          gc.setFont(new Font("", 20));
          gc.fillText("PAUSED. Press ESC to unpause.", 100, 100);
        }
      }
    }
  }

  static good_fruit[] gFruits = new good_fruit[fruit_amount];
  static bad_fruit[] bFruits = new bad_fruit[fruit_amount];
  public static VBox vbox;
  public static GraphicsContext gc;
  public static Scene scene;
  public static boolean quit = true;
  public static Canvas canvas;
  static AnimationTimer timer;

  /**
   * Provides a static method to initialize game stage
   * 
   * @param stage
   */
  public static void init(Stage stage) {
    direction = Dir.up;
    gameOver = false;
    snake.clear();
    vbox = new VBox();
    vbox.setAlignment(Pos.CENTER);
    canvas = new Canvas(scaled_width, scaled_height);
    gc = canvas.getGraphicsContext2D();
    vbox.getChildren().add(canvas);
    scene = new Scene(vbox, scaled_width, scaled_height, border);
    stage.setScene(scene);
    stage.show();
    stage.setTitle("Snake");
    snake.add(new Box(width / 2, height / 2));
    snake.add(new Box(snake.get(0).x + 0, snake.get(0).y + 1));
    snake.add(new Box(snake.get(0).x + 0, snake.get(1).y + 1));
    for (int i = 0; i < fruit_amount; i++) {
      gFruits[i] = newGoodFruit();
      if (!disable_bad_fruits)
        bFruits[i] = newBadFruit();
    }
    play(stage);
    timer.start();
    return;
  }

  /**
   * Modular Function to handle the playing of the game.
   * 
   * @param stage
   */
  public static void play(Stage stage) {
    timer = new MyTimer();
    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      /**
       * Detects and Handles keyboard input to update Snake's direction.
       * 
       * @param key
       */
      public void handle(KeyEvent key) {
        KeyCode kc = key.getCode();
        if ((kc == KeyCode.W || kc == KeyCode.UP || kc == KeyCode.K) && direction != Dir.down && !Paused) {

          direction = Dir.up;

        }
        if ((kc == KeyCode.S || kc == KeyCode.DOWN || kc == KeyCode.J) && direction != Dir.up && !Paused) {
          direction = Dir.down;

        }
        if ((kc == KeyCode.A || kc == KeyCode.LEFT || kc == KeyCode.H) && direction != Dir.right && !Paused) {
          direction = Dir.left;
        }
        if ((kc == KeyCode.D || kc == KeyCode.RIGHT || kc == KeyCode.L) && direction != Dir.left && !Paused ) {
          direction = Dir.right;
        }
        if ((kc == KeyCode.P) ) {
          direction = Dir.up;
          direction = Dir.right;

        }
        if (kc == KeyCode.ESCAPE)
          Paused = !Paused;
        if (kc == KeyCode.SPACE && gameOver) {
          Paused = true;
          timer.stop();
          stage.close();
          init(new Stage());
        }

      }
    });
    return;
  }

  @Override
  public void start(Stage stage) {
    init(stage);

  }

  /**
   * Provides updates for Snake to calculate logic.
   * Detects collisions, resulting in a game over.
   * Detects fruit consumptions, allows for spawn of new ones.
   * Updates position of snake, from head to tail.
   * 
   * @param gc to be passed on to child function Render()
   */
  public static void tick(GraphicsContext gc) {
    Box snake_head = snake.get(0);
    Box snake_part;
    if (gameOver) {
      gc.setFill(Color.RED);
      gc.setFont(new Font("", 50));
      gc.fillText("YOU SUCK", scaled_width / 2 - 100, scaled_height / 2);
      gc.setFont(new Font("", 25));
      gc.fillText("Press SPACE to restart", scaled_width / 2 - 105, scaled_height / 2 + 50);
      return;
    }
    Render(gc);

    

    // Checking if the Snake Collides With Itself
    for (int i = 1; i < snake.size(); i++) {
      snake_part = snake.get(i);
      if (snake_head.x == snake_part.x && snake_head.y == snake_part.y) {
        // gc.fillText(snake_head.x +  ":" + snake_head.y + " => " + snake_part.x + ":" + snake_part.y, 100, 100 );
        gameOver = true;
      }
    }

    // Moving the snake on screen. Start from the tail, and move it "up" by one.
    // Cool note: This is done after the check because if we move the snake up by
    // one then check, we are gonna get true every time.
    // Resulting in a funny at first but rapidly annoying instant game over screen.
    for (int i = snake.size() - 1; i >= 1; i--) {
      snake.get(i).x = snake.get(i - 1).x;
      snake.get(i).y = snake.get(i - 1).y;
    }

    // Checking if the snake collides with the border, and also updating the
    // position of the head
    //

    gameOver = (direction == Dir.up && (--snake_head.y < 0)
        || direction == Dir.down && (++snake_head.y >= height)
        || direction == Dir.left && (--snake_head.x < 0)
        || direction == Dir.right && (++snake_head.x >= width)
        || gameOver);

    // try {
    //   // This is to avoid this dumb bug that makes the snake change directions twice
    //   // on the same frame. Resulting in game over
    //   Thread.sleep(30);
    // } catch (InterruptedException e) {
    //   e.printStackTrace();
    // }

    // Update the position of the snake
    // if (direction == Dir.up) --snake_head.y;
    // if (direction == Dir.down) ++snake_head.y;
    // if (direction == Dir.left) --snake_head.x;
    // if (direction == Dir.right) ++snake_head.x;

    // gameOver = snake_head.y < 0;
    // gameOver = snake_head.x < 0;
    // gameOver = snake_head.y < height;
    // gameOver = snake_head.x < width;

    if (snake.size() < 3) {
      gameOver = true;
    }

    // Checking if we ate a good fruit
    for (int i = 0; i < fruit_amount; i++) {
      if (snake_head.x == gFruits[i].x && snake_head.y == gFruits[i].y) {
        snake.add(new Box(-1, -1));
        gFruits[i] = newGoodFruit();
      }
    }
    // Checking if we ate a bad fruit
    if (!disable_bad_fruits) {
      for (int i = 0; i < fruit_amount; i++) {
        if (snake_head.x == bFruits[i].x && snake_head.y == bFruits[i].y) {
          snake.remove(snake.size() - 1);
          bFruits[i] = newBadFruit();
        }
      }
    }
  }

  /**
   * Provides the rendering from the logic results of TicK() as well as user
   * defined settings.
   * Performs logical calculations of high_score and updates its value, if needed.
   * 
   * @param gc allows for drawing onto graphics context.
   */
  public static void Render(GraphicsContext gc) {
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if ((i + j) % 2 == 0) {
          gc.setFill(game_background);
        } else {
          gc.setFill(game_background1);
        }
        gc.fillRect(i * box_Size, j * box_Size, box_Size, box_Size);
      }
    }

    current_score = snake.size() - 3;
    gc.setFill(Color.BLACK);
    gc.setFont(new Font("", 20));
    gc.fillText("Score: " + current_score, 10, 20);
    gc.fillText("High Score: " + high_score, 10, 40);
    // gc.fillText("Head: " + snake.get(0).x + ":" + snake.get(0).y, 10, 60);


    try {
      RandomAccessFile file = new RandomAccessFile("Score.txt", "rw");
      file.seek(0);
      try {
        high_score = file.readInt();
      } catch (Exception e) {
        high_score = 0;
      }
      if (high_score < current_score) {
        file.seek(0);
        file.writeInt(current_score);
      }
      file.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
    gc.setFill(snake_head);
    gc.fillRect(snake.get(0).x * box_Size, snake.get(0).y * box_Size, box_Size - 1, box_Size - 1);
    gc.setFill(snake_color);

    for (int i = 1; i < snake.size(); i++) {
      gc.fillRect(snake.get(i).x * box_Size, snake.get(i).y * box_Size, box_Size - 1, box_Size - 1);
    }

    gc.setFill(good_fruit);
    for (good_fruit f : gFruits) {
      gc.fillOval(f.x * box_Size, f.y * box_Size, box_Size, box_Size);
    }

    if (!disable_bad_fruits) {
      gc.setFill(bad_fruit);
      for (bad_fruit f : bFruits) {
        gc.fillOval(f.x * box_Size, f.y * box_Size, box_Size + 1, box_Size + 1);
      }
    }
  }

  /**
   * Generates a new good_fruit at a random position, while also ensuring that the
   * fruit will not spawn on the Snake.
   * 
   * @return
   */
  public static good_fruit newGoodFruit() {
    while (true) {
      int x = rand.nextInt(width);
      int y = rand.nextInt(height);
      for (Box c : snake) {
        if ((c.x != x && c.y != y))
          return new good_fruit(x, y);
      }
    }
  }

  /**
   * Generates a new bad_fruit at a random position, while also ensuring that the
   * fruit will not spawn on the Snake.
   * 
   * @return
   */
  public static bad_fruit newBadFruit() {
    while (true) {
      int x = rand.nextInt(width);
      int y = rand.nextInt(height);
      for (Box c : snake) {
        if (!(c.x == x && c.y == y))
          return new bad_fruit(x, y);
      }
    }
  }

  public static void main(String args[]) {
    launch(args);
  }

}
