package NeuralNetwork;

/**
 * Created by warren on 1/16/15.
 */
public class OutputLayer extends Layer {

    public OutputLayer(int neurons) {
        outgoingWeights = null;

        outputs = new double[neurons]; // +1 for bias
        net = new double[neurons];
        delta = new double[neurons];
    }

    public void feedForward(Layer prevLayer) {
        // TODO error checking

        for (int j = 0; j < net.length; j++) {
            net[j] = 0;

            for (int i = 0; i < prevLayer.outputs.length; i++) {
                net[j] += prevLayer.outputs[i] * prevLayer.outgoingWeights[i][j];
            }

            outputs[j] = sigma(net[j]);
        }
    }

    public void updateWeights(double[] target, double rate) {

        // No bias neuron--this is the output layer
        for (int j = 0; j < outputs.length; j++) { // For each neuron, j, of this layer
            delta[j] = (outputs[j] - target[j]) * sigmaPrime(net[j]);
        }
    }
}
