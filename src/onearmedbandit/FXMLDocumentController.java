package onearmedbandit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.concurrent.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;

/**
 * FXML Controller class
 *
 * @author Oliver
 */
public class FXMLDocumentController implements Initializable
{

    @FXML
    private ImageView slot1;
    @FXML
    private ImageView slot2;
    @FXML
    private ImageView slot3;
    @FXML
    private ImageView armup;
    @FXML
    private ImageView armdown;
    @FXML
    private ImageView jackpot;

    @FXML
    private Polygon button1;
    @FXML
    private Polygon button2;
    @FXML
    private Polygon button3;

    public Image[] fruits;

    private final int AMOUNT_OF_FRUIT_IMAGES = 9;

    private boolean lockArm = false;

    public ExecutorService executer;

    private boolean[] columnStopper = new boolean[]
    {
        false, false, false
    };

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        fruits = IntStream.range(0, AMOUNT_OF_FRUIT_IMAGES).mapToObj(val -> new Image(getClass().getResourceAsStream("images/fruits" + val + ".png"))).toArray(size -> new Image[size]);
        slot1.setImage(fruits[(int) (Math.random() * fruits.length)]);
        slot2.setImage(fruits[(int) (Math.random() * fruits.length)]);
        slot3.setImage(fruits[(int) (Math.random() * fruits.length)]);

        button1.setUserData(0);
        button2.setUserData(1);
        button3.setUserData(2);

        button1.setOnMouseClicked(e -> stopColumn(e));
        button2.setOnMouseClicked(e -> stopColumn(e));
        button3.setOnMouseClicked(e -> stopColumn(e));
    }

    @FXML
    public void close()
    {
        System.exit(0);
    }

    @FXML
    public void pull()
    {
        //ArmAnimation
        armup.setVisible(false);
        armup.setDisable(true);
        armdown.setVisible(true);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(500);
                    Platform.runLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            armup.setVisible(true);
                            armup.setDisable(false);
                            armdown.setVisible(false);
                        }
                    });
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        //ArmAnimation end

        if (!lockArm)
        {
            lockArm = true;

            List<Future<?>> futures = new ArrayList<Future<?>>();

            //Start Spin
            executer = Executors.newFixedThreadPool(3, new DaemonThreadFactory());
            //Adding runnables and assigning them a
            Future<?> f1 = executer.submit(new spinnerRunnable(slot1, 0));
            Future<?> f2 = executer.submit(new spinnerRunnable(slot2, 1));
            Future<?> f3 = executer.submit(new spinnerRunnable(slot3, 2));

            futures.add(f1);
            futures.add(f2);
            futures.add(f3);

            columnStopper = new boolean[]
            {
                false, false, false
            };
            
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        //Block until all threads are done
                        for (Future<?> future : futures)
                        {
                            future.get();
                        }
                        checkResults();
                        executer.shutdown();
                        executer = null;
                        lockArm = false;
                    } catch (ExecutionException ex)
                    {
                        lockArm = false;
                        ex.printStackTrace();
                    } catch (InterruptedException ex)
                    {
                        lockArm = false;
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @FXML
    public void stopColumn(MouseEvent e)
    {
        columnStopper[(int) ((Polygon) e.getSource()).getUserData()] = true;
    }

    @FXML
    public void closeJackpot()
    {
        Platform.runLater(() -> jackpot.setVisible(false));
    }

    private void checkResults()
    {
        if (slot1.getImage() == slot2.getImage() && slot2.getImage() == slot3.getImage())
        {
            Platform.runLater(() -> jackpot.setVisible(true));
        }
    }

    public class spinnerRunnable implements Runnable
    {

        private ImageView imageView;
        private final int id;

        public spinnerRunnable(ImageView imageView, int id)
        {
            this.imageView = imageView;
            this.id = id;
        }

        @Override
        public void run()
        {

            int counter = 0;
            int offset = (int) (Math.random() * AMOUNT_OF_FRUIT_IMAGES);
            int speed = (int) (Math.random() * 9 + 2);
            try
            {
                while (counter < 50 && !columnStopper[id])
                {
                    final int finalIntegerForFunctionalInterfaceThatHasANastyRequirement = (counter + offset) % fruits.length;
                    Platform.runLater(() -> imageView.setImage(fruits[finalIntegerForFunctionalInterfaceThatHasANastyRequirement]));
                    Thread.sleep(20 + (speed * counter));
                    counter++;
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    static class DaemonThreadFactory implements ThreadFactory
    {

        public Thread newThread(Runnable r)
        {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    }
    
    @FXML
    public void benjamin()
    {
        try
        {
            Desktop.getDesktop().browse(new URL("https://www.youtube.com/watch?v=dQw4w9WgXcQ").toURI());
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
