package NeuralNetwork;

/**
 * Created by warren on 1/16/15.
 */
public class InputLayer extends Layer {

    public InputLayer(int neurons, int outgoingNeurons) {
        super(neurons, outgoingNeurons);

        delta = null;
    }

    public void feedForward(double[] input) {
        if (input.length != outputs.length - 1) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < input.length; i++) {
            outputs[i] = input[i];
        }
    }

    public void feedForward(Layer l) {
        throw new UnsupportedOperationException();
    }

    public void updateWeights(Layer nextLayer, double rate) {

        for (int i = 0; i < outgoingWeights.length; i++) {
            for (int j = 0; j < outgoingWeights[i].length; j++) {
                outgoingWeights[i][j] -= rate * outputs[i] * nextLayer.delta[j];
            }
        }
    }

}