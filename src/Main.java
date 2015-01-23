import NeuralNetwork.NeuralNet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Trains an Artificial Neural Network and classifies incoming data from a
 * socket.
 */
public class Main {

    /**
     * Names of files that contain data obtained from UCI machine learning
     * data sets.
     */
    public final String[] FILENAMES = {"pendigits-orig.tra", "training.txt"};

    private NeuralNet ann;

    public Main() {
        ann = new NeuralNet(new int[]{40, 10});
        train(ann);

        Runnable task = () -> { listen(); };
        (new Thread(task)).start();
    }

    /**
     * This should be a one-time operation.
     * @return
     */
    private List<Digit> readFiles() {
        Stack<Digit> digits = new Stack<Digit>();

        for (int i = 0; i < FILENAMES.length; i++) {
            System.out.println("NEW FILE");

            BufferedReader reader = null;
            try {
                String path = System.getProperty("user.home") + "/Documents/ann_data/" + FILENAMES[i];
                reader = new BufferedReader(new FileReader(path));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    parseLine(line, digits, i == 0);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return digits;
    }

    private void parseLine(String line, Stack<Digit> digits, boolean flip) {
        if (line.startsWith(".SEGMENT")) { // new digit
            int index1 = line.indexOf('\"');
            int index2 = line.lastIndexOf('\"');
            String number = line.substring(index1 + 1, index2);
            int i = Integer.parseInt(number);

            digits.add(new Digit(i));
        } else if (line.startsWith(".PEN_DOWN")) {
            digits.peek().addPath();
        } else if (line.startsWith(" ")) {
            // TODO check if the origin is in the top-left
            int index = line.lastIndexOf(' ');

            //System.out.println(startIndex + " | " + line);
            int endIndex = line.charAt(index - 1) == ' ' ? index - 1 : index;


            float x = Float.parseFloat(line.substring(0, endIndex));
            float y = Float.parseFloat(line.substring(index + 1));

            if (flip) y *= -1;

            digits.peek().addPoint(x, y);
        }
    }

    private void train(NeuralNet ann) {
        List<Digit> training = new LinkedList<Digit>(), testing = new LinkedList<Digit>();
        List<Digit> symbols = readFiles();
        Collections.shuffle(symbols);

        for (Digit d : symbols) {

            if (Math.random() < 0.6) {
                training.add(d);
            } else {
                testing.add(d);
            }
        }


        int i = 0, misclassified = 0;

        for (Digit d : training) {
            ann.feedForward(d.getFeatureVector());
            ann.backProp(d.getOutputVector());

            if (i % 50 == 0) { // For every 50 digits, print the accuracy
                misclassified = 0;
                for (Digit a : testing) {
                    ann.feedForward(a.getFeatureVector());
                    if (a.getDigit() != ann.getClassification()) {
                        misclassified++;
                    }

                }
                // Prints out the accuracy of the model.
                System.out.printf(misclassified + " | " + testing.size() + " | %2.2f%% \n", (100 - 100 * (0f + misclassified) / testing.size()));
            }
            i++;
        }

        System.out.println(misclassified);

    }

    private void listen() {
        System.out.println("listening");

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8888); // Server socket

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Server started. Listening to the port 8888");

        Socket clientSocket = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String message = null;

        while(true) {
            try {
                clientSocket = serverSocket.accept(); // accept the client connection
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader); // get the client message
                message = bufferedReader.readLine();

                System.out.println(message);
                classify(parseDigitFromCsv(message), ann);
                inputStreamReader.close();
                clientSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates a Digit object from string of comma separated values. The format
     * should be
     *      x_0, y_0, x_1, y_1, ..., !, ..., x_n, y_n, ...
     * where (x_k, y_k) is a coordinate of the pen and ! is a pen lift.
     */
    private Digit parseDigitFromCsv(String csv) {
        String[] values = csv.split(",");
        Digit result = new Digit();

        for (int i = 0; i < values.length; i += 2) {
            float x, y;
            try {
                x = Float.parseFloat(values[i]);
            } catch (NumberFormatException e) {
                result.addPath();
                i++;
                continue;
            }
            y = Float.parseFloat(values[i + 1]); // Should not throw an exception
            result.addPoint(x, y);
        }
        return result;
    }

    /**
     * Classifies
     * @param digit
     * @param ann
     */
    private void classify(Digit digit, NeuralNet ann) {
        double[] features = digit.getFeatureVector();
        ann.feedForward(features);
        double[] output = ann.getOutput();

        // Prints out the classification of the given digit
        System.out.println("Digit is a " + ann.getClassification());
        for (int j = 0; j < output.length; j++) {
            System.out.printf("[" + j + "]: %2.2f%%\n", 100 * (1 + output[j])/2);
        }
        digit.setDigit(ann.getClassification());
    }


    public static void main(String[] args) {
        new Main();
    }
}
