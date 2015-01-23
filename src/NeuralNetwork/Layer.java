package NeuralNetwork;


/**
 * Created by warren on 1/11/15.
 */
public class Layer {

    /**
     * Weights of the outgoing edges.
     *
     * weights[i][j] = weight between the ith neuron of this layer and the jth neuron of the next
     *                 layer;
     */
    protected double[][] outgoingWeights; //
    protected double[] outputs;
    protected double[] net;
    protected double[] delta;

    public Layer(){}; // TODO delete

    /**
     *
     * @param neurons Number of neurons in this layer.
     * @param outgoingNeurons Number of neurons in the next layer
     */
    public Layer(int neurons, int outgoingNeurons) {
        outputs = new double[neurons + 1];  // +1 for bias
        net = new double[neurons];          // weighted sum of the input of each neuron
        delta = new double[neurons];

        outputs[neurons] = 1;   // bias neuron output

        outgoingWeights = new double[neurons + 1][outgoingNeurons];

        for (int i = 0; i < outgoingWeights.length; i++) {
            for (int j = 0; j < outgoingWeights[i].length; j++) {
                outgoingWeights[i][j] = Math.random() - 0.5;
            }
        }
    }

    /**
     *
     * @param prevLayer
     */
    public void feedForward(Layer prevLayer) {
        double[] inputs = prevLayer.getOutputs();
        double[][] incomingWeights = prevLayer.getOutgoingWeights();

        // excludes bias
        for (int j = 0; j < net.length; j++) { // For each neuron j of this layer
            net[j] = 0;

            for (int i = 0; i < inputs.length; i++) {
                net[j] += inputs[i] * incomingWeights[i][j];
            }

            outputs[j] = sigma(net[j]);
        }
    }

    public void updateWeights(Layer nextLayer, double rate) {
        for (int j = 0; j < delta.length; j++) { // For each neuron, j, of this layer

            delta[j] = 0;

            for (int k = 0; k < nextLayer.delta.length; k++) {
                delta[j] += nextLayer.delta[k] * outgoingWeights[j][k];
            }

            delta[j] *= sigmaPrime(net[j]);
        }

        //prevLayer.updateWeights(this, rate);

        for (int i = 0; i < outgoingWeights.length; i++) {
            for (int j = 0; j < outgoingWeights[i].length; j++) {
                outgoingWeights[i][j] -= rate * outputs[i] * nextLayer.delta[j];
            }
        }
    }

    /**
     * Returns the outputs (including bias).
     * @return
     */
    public double[] getOutputs() {
        return outputs;
    }

    public double[][] getOutgoingWeights() {
        return outgoingWeights;
    }

    /**
     * Returns the number of neurons in this layer (including bias)
     * @return
     */
    public int size() {
        return outputs.length;
    }

    protected static double sigma(double x) {
        return Math.tanh(x);
    }

    protected static double sigmaPrime(double x) {
        double sigma = sigma(x);
        return 1 - sigma * sigma; // d/dx tanh(x) = 1 - tanh(x)^2
    }
}
