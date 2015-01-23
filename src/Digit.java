import java.util.ArrayList;
import java.util.List;
import java.lang.Float;

/**
 * Created by warren on 1/18/15.
 */
public class Digit {
    public int digit;

    List<List<Float>> paths;
    List<Float> currentPath;

    public Digit() {
        paths = new ArrayList<List<Float>>();
        currentPath = new ArrayList<Float>();
        paths.add(currentPath);
        digit = -1;
    }

    public Digit(List<Float> path) {
        this();
        paths.add(path);
    }

    public Digit(List<Float> path, int digit) {
        this(path);
        this.digit = digit;
    }

    public Digit(int digit) {
        this();
        this.digit = digit;
    }

    public void addPoint(float x, float y) {
        currentPath.add(x);
        currentPath.add(y);
    }

    public void addPath() {
        if (currentPath == null || currentPath.size() != 0) {
            currentPath = new ArrayList<Float>();
            paths.add(currentPath);
        }
    }

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public double[] getOutputVector() {
        double[] result = new double[10];

        for (int i = 0; i < 10; i++) {
            result[i] = (i == digit) ? 1 : -1;
        }
        return result;
    }

    public double[] getFeatureVector() {
        int size = 16, orientations = 8, counter = 0;
        double[] spacialFeatures = getSpacialFeatures(size),
                 directionalFeatures = getOrientedFeatures(orientations);
        double[] features = new double[size * 2 + orientations];

        // TODO optimize
        for (int i = 0; i < spacialFeatures.length; i++) {
            features[counter++] = spacialFeatures[i];
        }
        for (int i = 0; i < directionalFeatures.length; i++) {
            features[counter++] = directionalFeatures[i];
        }
        return features;
    }

    public double[] getSpacialFeatures(int size) {
        double[] features = new double[size * 2];
        boolean[][] bitmap = toBitmap(size);

        // Column-row histograms
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (bitmap[i][j]) {
                    features[j]++;
                    features[size + i]++;
                }
            }
        }

        for (int i = 0; i < size * 2; i++) {
            features[i] /= size;
        }

        return features;
    }

    /**
     * Returns a list
     * @param orientations The number of principle orientations. For example, if
     *                     this value is four, then this corresponds to North, South,
     *                     East, and West.
     */
    public double[] getOrientedFeatures(int orientations) {
        double[] features = new double[orientations];
        double length = 0;

        for (List<Float> path : paths) {
            for (int i = 0; i < path.size() - 2; i += 2) {
                double dx = path.get(i + 2) - path.get(i),
                       dy = path.get(i + 3) - path.get(i + 1);

                double angle = Math.atan2(dy, dx);
                if (angle < 0) {
                    angle += 2 * Math.PI;
                }

                int orientation = (int)(((angle / (2 * Math.PI)) * orientations) + 0.5);

                double dist = Math.sqrt(dx * dx + dy * dy);

                features[orientation % orientations] += dist;
                length += dist;
            }
        }

        // Normalize the features;
        for (int i = 0; i < features.length; i++) {
            features[i] /= length;
        }
        return features;
    }

    public boolean[][] toBitmap(int size) {
        List<List<Float>> strokes = normalize(size - 1);
        boolean[][] bitmap = new boolean[size][size];

        for (List<Float> stroke : strokes) {
            for (int i = 0; i < stroke.size() - 2; i += 2) {

                int x0 = (int)(stroke.get(i) + .5),
                    y0 =  (int)(stroke.get(i + 1) + 0.5),
                    x1 =  (int)(stroke.get(i + 2) + .5),
                    y1 =  (int)(stroke.get(i + 3) + 0.5);
                lineTo(bitmap, x0, y0, x1, y1);
            }
        }

        return bitmap;
    }

    /**
     *
     * @param size
     * @return
     */
    private List<List<Float>> normalize(int size) {
        // Initialize min, max points of all the strokes
        float minX = paths.get(0).get(0),
                maxX = minX,
                minY = paths.get(0).get(1),
                maxY = minY;

        // Find the minimum and max points of all strokes
        for (List<Float> path : paths) {
            for (int i = 0; i < path.size(); i += 2) {
                float x = path.get(i), y = path.get(i + 1);

                if (x < minX) {
                    minX = x;
                } else if (x > maxX) {
                    maxX = x;
                }

                if (y < minY) {
                    minY = y;
                } else if (y > maxY) {
                    maxY = y;
                }
            }
        }

        // Normalize points
        float length = Math.max(maxX - minX, maxY - minY),
              offsetX = (length - (maxX - minX))/2,
              offsetY = (length - (maxY - minY))/2;

        List<List<Float>> newPaths = new ArrayList<List<Float>>(paths.size());

        for (List<Float> path : paths) {
            List<Float> newPath = new ArrayList<Float>(path.size());

            for (int i = 0; i < path.size(); i += 2) {
                newPath.add((size * (path.get(i) - minX + offsetX)) / length);
                newPath.add((size * (path.get(i + 1) - minY + offsetY)) / length);
            }

            newPaths.add(newPath);
        }

        return newPaths;
    }

    /**
     * Draws a line on the given bitmap using Bresenhams's algorithm
     * @param bitmap
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     */
    private void lineTo(boolean[][] bitmap, int x0, int y0, int x1, int y1) {

        int incX = x0 < x1 ? 1 : -1,
                incY = y0 < y1 ? 1 : -1;

        int dx = x1 - x0, dy = y1 - y0;

        if (dx == 0) { // vertical line, special case
            for (; y0 != y1 + incY; y0 += incY) {
                bitmap[x0][y0] = true;
            }
            return;
        }

        // Else
        double m = Math.abs(((double)dy) / dx),
                error = 0;

        for ( ; x0 != x1 + incX; x0 += incX) {
            bitmap[x0][y0] = true;
            error += m;

            while (error > 0.5 && y0 != y1 + incY) {
                bitmap[x0][y0] = true;
                y0 += incY;
                error--;
            }
        }
    }

    private String toCsv() {
        String result = digit == -1 ? "" : digit + ",";

        for (List<Float> path : paths) {
            for (Float f : path) {
                result += f + ",";
            }
            result += "!,";
        }
        return result;
    }

}
