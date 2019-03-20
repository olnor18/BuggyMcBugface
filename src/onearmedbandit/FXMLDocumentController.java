package onearmedbandit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 * FXML Controller class
 *
 * @author Oliver
 */
public class FXMLDocumentController implements Initializable {

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

    public Image[] fruits;

    private final int AMOUNT_OF_FRUIT_IMAGES = 9;

    private boolean lockArm = false;
    
    public ExecutorService executer;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fruits = IntStream.range(0, AMOUNT_OF_FRUIT_IMAGES).mapToObj(val -> new Image(getClass().getResourceAsStream("images/fruits" + val + ".png"))).toArray(size -> new Image[size]);
        slot1.setImage(fruits[(int) (Math.random() * fruits.length)]);
        slot2.setImage(fruits[(int) (Math.random() * fruits.length)]);
        slot3.setImage(fruits[(int) (Math.random() * fruits.length)]);
    }

    @FXML
    public void pull() {
        System.out.println("spin");
        //ArmAnimation
        armup.setVisible(false);
        armup.setDisable(true);
        armdown.setVisible(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            armup.setVisible(true);
                            armup.setDisable(false);
                            armdown.setVisible(false);
                        }
                    });
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        //ArmAnimation end

        if (!lockArm) {
            lockArm = true;

            List<Future<?>> futures = new ArrayList<Future<?>>();

            //Start Spin
            executer = Executors.newFixedThreadPool(3, new DaemonThreadFactory());
            Future<?> f1 = executer.submit(new spinnerRunnable(slot1));
            Future<?> f2 = executer.submit(new spinnerRunnable(slot2));
            Future<?> f3 = executer.submit(new spinnerRunnable(slot3));

            futures.add(f1);
            futures.add(f2);
            futures.add(f3);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (Future<?> future : futures) {
                            future.get();
                        }
                        System.out.println("Done!");
                        executer.shutdown();
                        executer = null;
                        lockArm = false;
                    } catch (ExecutionException ex) {
                        lockArm = false;
                        ex.printStackTrace();
                    } catch (InterruptedException ex) {
                        lockArm = false;  
                        ex.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public class spinnerRunnable implements Runnable {

        private ImageView imageView;

        public spinnerRunnable(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void run() {
            int counter = 0;
            int offset = (int) (Math.random() * AMOUNT_OF_FRUIT_IMAGES);
            System.out.println(offset + "---" +  imageView.getId());
            try {
                while (true) {
                    imageView.setImage(fruits[(counter + offset) % AMOUNT_OF_FRUIT_IMAGES]);
                    Thread.sleep(100 + (10 * counter));
                    if (counter > 40) {
                        return;
                    }
                    counter++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    static class DaemonThreadFactory implements ThreadFactory {
     public Thread newThread(Runnable r) {
         Thread thread = new Thread(r);
         thread.setDaemon(true);
         thread.setPriority(1);
         return thread;
     }
 }
}
