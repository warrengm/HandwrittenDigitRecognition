package NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

public class NeuralNet {

    /**
     *
     */
    List<Layer> layers;

    /**
     *
     * @param topology the zero-th element corresponds with the input layer.
     */
    public NeuralNet(int[] topology) {
        // TODO check topology for errors

        layers = new ArrayList<Layer>(topology.length);

        layers.add(new InputLayer(topology[0], topology[1]));

        for (int i = 1; i < topology.length - 1; i++) {
            layers.add(new Layer(topology[i], topology[i + 1]));
        }

        layers.add(new OutputLayer(topology[topology.length - 1]));
    }

    public void feedForward(double[] input) {
        if (input.length != layers.get(0).size() - 1) {
            throw new IllegalArgumentException("wrong input size");
        }

        Layer prevLayer = layers.get(0), current = null;
        ((InputLayer)prevLayer).feedForward(input);

        for (int i = 1; i < layers.size(); i++) {
            current = layers.get(i);
            current.feedForward(prevLayer);
            prevLayer = current;
        }
    }

    public void train(int[][] inputs, int[][] targets) {
        if (inputs.length != targets.length) {
            throw new IllegalArgumentException();
        }

    }

    public void backProp(double[] targets) {
        double[] outputs = layers.get(layers.size() - 1).getOutputs();

        if (targets.length != outputs.length) { // No bias neuron
            throw new IllegalArgumentException("length");
        }

        ((OutputLayer)layers.get(layers.size() - 1)).updateWeights(targets, 0.2);
        for (int i = layers.size() - 2; i >= 0; i--) {
            layers.get(i).updateWeights(layers.get(i + 1), 0.2);
        }

    }

    public double[] getOutput() {
        return layers.get(layers.size() - 1).getOutputs();
    }

    public int getClassification() {
        int maxIndex = 0;
        double[] output = getOutput();

        for (int i = 1; i < output.length; i++) {
            if (output[i] > output[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
