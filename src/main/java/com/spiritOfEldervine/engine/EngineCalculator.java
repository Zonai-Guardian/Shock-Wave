package com.spiritOfEldervine.engine;

// Imports for custom objects
import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.Direction4;
import com.spiritOfEldervine.networking.PacketManager.PacketDataType;
import com.spiritOfEldervine.networking.PacketManager.PacketPurpose;
// Imports for maven dependencies
// Imports for Jackson (Json Stuff)
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// Imports for Universal Tween Engine
import aurelienribon.tweenengine.TweenEquation;
import aurelienribon.tweenengine.TweenEquations;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class EngineCalculator {
    private static Random random = new Random();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public EngineCalculator() {}
    
    public static void printStackTrace() {
        new Exception().printStackTrace();
    }
    public static void printExceptionInfo(Exception e) {
        System.out.println("Message:\n" + e.getMessage() + "\nCause:\n" + e.getCause() + "\nStack Trace:\n");
        e.printStackTrace();
    }

    public static Direction4 getOppositeDirection(Direction4 originalDirection) {
        if (originalDirection == null) {
            return null;
        }
        switch (originalDirection) {
            case RIGHT:
                return Direction4.LEFT;
            case LEFT:
                return Direction4.RIGHT;
            case TOP:
                return Direction4.BOTTOM;
            case BOTTOM:
                return Direction4.TOP;
            case CENTER:
                return Direction4.CENTER;
            default:
                return null;
        }
    }

    // Unit Conversions
    public static double framesToSeconds(int frames) {
        return frames / Game.TARGET_UPS; //this uses the target updates/second (fps) from the Game class in claculations.
    }
    public static int secondsToFrames(double seconds) {
        return (int)(seconds * Game.TARGET_UPS); //this uses the target updates/second (fps) from the Game class in claculations.
    }


    // Random-Related Methods
    public static int randomInt(int bound) {
        if (bound == 0) {return 0;} 
        if (bound > 0) {
            return random.nextInt(bound);
        } else {
            return random.nextInt(bound * -1) * -1;
        }
    }
    public static Double randomDouble(Double bound) {
        if (bound == 0.0 || bound == 0) {return 0.0;} 
        if (bound > 0) {
            return random.nextDouble(bound);
        } else {
            return random.nextDouble(bound * -1) * -1;
        }
    }

    public static int randomRange(int minOriginal, int maxOriginal) {
        int min = Math.min(minOriginal, maxOriginal);
        int max = Math.max(minOriginal, maxOriginal);

        if (min == max) {
            return min;
        }

        return random.nextInt((max - min) + 1) + min;
    }
    public static Double randomRange(Double minOriginal, Double maxOriginal) {
        Double min = Math.min(minOriginal, maxOriginal);
        Double max = Math.max(minOriginal, maxOriginal);

        if (min.equals(max)) {
            return min;
        }

        return min + (Math.random() * (max - min));
    }
    public static int randomRange(Point range) {
        int min = Math.min(range.x, range.y);
        int max = Math.max(range.x, range.y);

        if (min == max) {
            return min;
        }

        return random.nextInt((max - min) + 1) + min;
    }
    public static Double randomRange(Point.Double range) {
        Double min = Math.min(range.x, range.y);
        Double max = Math.max(range.x, range.y);

        if (min.equals(max)) {
            return min;
        }

        return min + (Math.random() * (max - min));
    }
    public static Point getRandomPointInRect(Rectangle rect) {
        return new Point(randomRange(rect.x, rect.x + rect.width), randomRange(rect.y, rect.y + rect.height));
    }
    

    // Jackson Conversions
    public static String objectToJson(Object object, boolean prettyPrinting) {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            if (prettyPrinting) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return objectMapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert Object with class \"" + object.getClass() + "\" to json format in EngineCalculator.objectToJson()!");
            e.printStackTrace();
            return "[json parsing error]";
        }
    }
    public static Object jsonToObject(Class<?> objectClass, String jsonFormattedString) {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return objectMapper.readValue(jsonFormattedString, objectClass);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert json formatted string to object with class  \"" + objectClass.getName() + "\" in EngineCalculator.objectToJson()!");
            e.printStackTrace();
            return "[json parsing error]";
        }
    }

    public static int getMin(Integer[] ints) {
        int min = 0;
        boolean first = true;
        for (int i = 0; i < ints.length; i++) {
            if (first || ints[i] < min) {
                first = false;
                min = ints[i];
            }
        }
        return min;
    }
    public static int getMax(Integer[] ints) {
        int max = 0;
        boolean first = true;
        for (int i = 0; i < ints.length; i++) {
            if (first || ints[i] > max) {
                first = false;
                max = ints[i];
            }
        }
        return max;
    }
    public static boolean areAllEqual(Integer[] ints) {
        if (ints.length == 0) {
            System.out.println("ints has a length of 0 in EngineCalculator.areAllEqual()!");
            EngineCalculator.printStackTrace();
            return false;
        }
        int num = ints[0];
        for (int i = 0; i < ints.length; i++) {
            if (ints[i] != num) {
                return false;
            }
        }
        return true;
    }
    public static Point rectToRangeX(Rectangle rect) {
        return new Point(rect.x, rect.x + rect.width);
    }
    public static Point rectToRangeY(Rectangle rect) {
        return new Point(rect.y, rect.y + rect.height);
    }
    public static Point intToPoint(int number) {
        return new Point(number, number);
    }
    public static Point.Double doubleToPoint(Double number) {
        return new Point.Double(number, number);
    }
    public static Point putPointInRect(Point point, Rectangle rect) {
        Point returnPoint = new Point(point.x, point.y);
        returnPoint.x = Math.min(rect.x, Math.max(rect.x + rect.width, point.x));
        returnPoint.y = Math.min(rect.y, Math.max(rect.y + rect.height, point.y));
        return returnPoint;
    }
    public static Point.Double putPointInRect(Point.Double point, Rectangle rect) {
        Point.Double returnPoint = new Point.Double(point.x, point.y);
        returnPoint.x = Math.min(rect.x, Math.max(rect.x + rect.width, point.x));
        returnPoint.y = Math.min(rect.y, Math.max(rect.y + rect.height, point.y));
        return returnPoint;
    }
    public static Rectangle putRectInRect(Rectangle small, Rectangle large) {
        return new Rectangle(
            Math.min(Math.max(small.x, large.x), large.x + large.width - small.width),
            Math.min(Math.max(small.y, large.y), large.y + large.height - small.height),
            small.width,
            small.height
        );
    }
    public static Double getOppositeOfDirection(Double direction) {
        direction += 180;
        if (direction > 360) {
            direction -= 360;
        }
        return direction;
    }
    public static Double roundToPlaces(Double value, int decimalPlaces) {
        double largeValue = Math.pow(10, decimalPlaces) * value;
        int largeInt = (int) largeValue;
        if (largeValue - largeInt >= 0.5) {
            largeInt++;
        }
        Double returnValue = largeInt / Math.pow(10, decimalPlaces);
        return returnValue;
    }
    public static Point rotatePoint(Point center, Point point, double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees);
        
        // Translate point to origin
        double translatedX = point.x - center.x;
        double translatedY = point.y - center.y;

        // Rotate the point
        double rotatedX = translatedX * Math.cos(angleRadians) - translatedY * Math.sin(angleRadians);
        double rotatedY = translatedX * Math.sin(angleRadians) + translatedY * Math.cos(angleRadians);

        // Translate back to original position
        double finalX = rotatedX + center.x;
        double finalY = rotatedY + center.y;

        return new Point((int)(finalX), (int)(finalY));
    }

    public static ArrayList<Point> polygonToPoints(Polygon polygon) {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < polygon.npoints; i++) {
            points.add(new Point(polygon.xpoints[i], polygon.ypoints[i]));
        }
        return points;
    }
    public static Polygon pointsToPolygon(ArrayList<Point> points) {
        Polygon poly = new Polygon();
        for (Point point : points) {
            poly.addPoint(point.x, point.y);
        }
        return poly;
    }
    public static Polygon rotatePolygonAroundPoint(Polygon polygon, Point center, Double degrees) {
        ArrayList<Point> newPoints = new ArrayList<>();
        ArrayList<Point> oldPoints = polygonToPoints(polygon);
        for (Point point : oldPoints) {
            newPoints.add(rotatePoint(center, point, degrees));
        }
        return pointsToPolygon(newPoints);
    }
    public static Polygon getIntersection(Polygon poly, Rectangle rect) {
        if (poly == null || rect == null) {return null;}
        Area polyArea = new Area(poly);
        Area rectArea = new Area(rect);

        rectArea.intersect(polyArea);

        return areaToPolygon(rectArea);
    }
    public static Polygon areaToPolygon(Area area) {
        List<Point2D.Double> points = new ArrayList<>();
        PathIterator pi = area.getPathIterator(null);
        double[] coords = new double[6];
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            if (type != PathIterator.SEG_CLOSE) {
                points.add(new Point2D.Double(coords[0], coords[1]));
            }
            pi.next();
        }

        Polygon p = new Polygon();
        for (Point2D.Double pt : points) {
            p.addPoint((int) Math.round(pt.x), (int) Math.round(pt.y));
        }
        return p;
    }
    public static Polygon subtractPolysFromPoly(Polygon poly, ArrayList<Polygon> polys) {
        Area polyArea = new Area(poly);
        for (Polygon listPoly : polys) {
            polyArea.subtract(new Area(listPoly));
        }
        return areaToPolygon(polyArea);
    }
    public static Polygon subtractShapesFromPoly(Polygon poly, ArrayList<Shape> shapes) {
        Area polyArea = new Area(poly);
        for (Shape listShape : shapes) {
            polyArea.subtract(new Area(listShape));
        }
        return areaToPolygon(polyArea);
    }
    public static Polygon subtractArcsFromPoly(Polygon poly, ArrayList<Arc2D> arcs) {
        Area polyArea = new Area(poly);
        for (Arc2D listArc : arcs) {
            polyArea.subtract(new Area(listArc));
        }
        return areaToPolygon(polyArea);
    }
    public static Point getCenterOfPolygon(Polygon poly) {
        if (poly == null) {return null;}
        ArrayList<Point> points = polygonToPoints(poly);
        int xTotal = 0;
        int yTotal = 0;
        
        for (Point point : points) {
            xTotal += point.x;
            yTotal += point.y;
        }
        if (points.size() == 0) {return new Point(); }
        return new Point(xTotal / points.size(), yTotal / points.size());
    }
    public static Point getCenterOfRect(Rectangle rect) {
        return new Point((int)(rect.x + rect.getWidth() / 2), (int)(rect.y + rect.getHeight() / 2));
    }
    
    //makes first vertex at point and retains the same shape and rotation of original polygon
    //used for hitboxes following entities without creating new ones or rotating points a lot
    public static Polygon homePolygonToPoint(Point point, Polygon poly) {
        ArrayList<Point> points = polygonToPoints(poly);
        ArrayList<Point> newPoints = new ArrayList<>();
        int xOffset = point.x;
        int yOffset = point.y;
        for (Point editablePoint : points) {
            newPoints.add(new Point(editablePoint.x + xOffset, editablePoint.y + yOffset));
        }
        return pointsToPolygon(newPoints);
    }

    //for finding the angle that a point is rotated around another point
    public static Double getRotationAroundPoint(Point fromPoint, Point toPoint) {
        // Step 1: Get angle from player to mouse (0° is up, clockwise)
        double dx = toPoint.getX() - fromPoint.getX();
        double dy = toPoint.getY() - fromPoint.getY();
        double angleRad = Math.atan2(dx, -dy); // dx first, -dy to make 0° point up
        double angleDeg = Math.toDegrees(angleRad);
        if (angleDeg < 0) angleDeg += 360;
        return angleDeg;
    }
    public static Point rotateAndDistancePoint(Point basePosition, double angleDegrees, double distance) {
        // Step 3: Convert to radians and compute sword position
        double angleRad = Math.toRadians(90 - angleDegrees); // Adjust for compass-style rotation
        double x = basePosition.getX() + distance * Math.cos(angleRad);
        double y = basePosition.getY() - distance * Math.sin(angleRad);
        return new Point((int)(x), (int)(y));
    }
    public static Point2D.Double rotateAndDistancePoint(Point2D.Double basePosition, double angleDegrees, double distance) {
        // Step 3: Convert to radians and compute sword position
        Double angleRad = Math.toRadians(90 - angleDegrees); // Adjust for compass-style rotation
        Double x = basePosition.getX() + distance * Math.cos(angleRad);
        Double y = basePosition.getY() - distance * Math.sin(angleRad);
        return new Point2D.Double(x, y);
    }
    
    public static void print(Point p) {System.out.println("Point[x:" + p.x + ",y:" + p.y + "]");}
    public static void print(Rectangle r) {System.out.println("Rectangle[x:" + r.x + ",y:" + r.y + ",width:" + r.width + ",height:" + r.height + "]");}
    public static void print(Polygon poly) {
        System.out.print("Polygon[");
        boolean first = true;
        for (Point p : polygonToPoints(poly)) {
            if (first) {
                first = false;
            } else {
                System.out.print(",");
            }
            print(p);
        }
        System.out.print("]");
    }

    public static <T> ArrayList<T> toArrayList(T ... objects) {
        return EngineCalculator.arrayToArrayList(objects);
    }
    public static <T> ArrayList<T> arrayToArrayList(T[] array) {
        return new ArrayList<>(Arrays.asList(array));
    }
    public static int intTowardInt(int fromNumber, int toNumber, int incriment) {
        if (incriment == 0) { return fromNumber; }
        if (fromNumber > toNumber) {
            if (fromNumber < incriment) {
                return 0;
            } else {
                return fromNumber - incriment;
            }
        } else {
            if (fromNumber > -incriment) {
                return 0;
            } else {
                return fromNumber + incriment;
            }
        }
    }
    public static Double doubleTowardDouble(Double fromNumber, Double toNumber, Double incriment) {
        Double returnValue = 0.0;
        if (incriment == 0.0) {
            returnValue = fromNumber;
        } else if (fromNumber > toNumber) {
            if (fromNumber - incriment < toNumber) {
                returnValue = toNumber;
            } else {
                returnValue = fromNumber - incriment;
            }
        } else {
            if (fromNumber + incriment > toNumber) {
                returnValue = toNumber;
            } else {
                returnValue = fromNumber + incriment;
            }
        }
        //System.out.println("from number: " + fromNumber + ", to number: " + toNumber + ", incriment: " + incriment + ", returning: " + returnValue);
        return returnValue;
    }
    public static boolean doubleWithinDouble(Double double1, Double double2, Double distance) {
        if (Math.abs(double1 - double2) <= distance) {return true; }
        return false;
    }
    public static boolean intWithinInt(int int1, int int2, int distance) {
        if (Math.abs(int1 - int2) <= distance) {return true; }
        return false;
    }
    public static Integer roundIntDown(int number, int roundNumber) {
        return (roundNumber * ((int)(number / roundNumber)));
    }
    public static Integer roundInt(int number, int roundNumber) {
        return (roundNumber * (Math.round(number / roundNumber)));
    }
    public static Double getDistanceBetweenPoints(Point point1, Point point2) {
        int xDifference = Math.abs(point1.x - point2.x);
        int yDifference = Math.abs(point1.y - point2.y);
        return Math.sqrt((xDifference * xDifference) + yDifference * yDifference);
    }
    public static Double getDistanceBetweenPoints(Point.Double point1, Point.Double point2) {
        double xDifference = Math.abs(point1.x - point2.x);
        double yDifference = Math.abs(point1.y - point2.y);
        return Math.sqrt((xDifference * xDifference) + yDifference * yDifference);
    }
    public static boolean pointWithinIntSquare(Point point1, Point point2, int distanceX, int distanceY) {
        if ((Math.abs(point1.x - point2.x) <= distanceX) && (Math.abs(point1.y - point2.y) <= distanceY)) { return true; }
        return false;
    }    
    public static  BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        // Create output image
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        // Draw scaled instance
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2d.drawImage(img, 0, 0, newW, newH, null);
        g2d.dispose();

        return resized;
    }
    public static BufferedImage rotateImage(BufferedImage img, double angle) {
        // Convert angle to radians
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        // Calculate new dimensions
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        // Create output image
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, img.getType());
        Graphics2D g2d = rotated.createGraphics();

        // Set up transform: rotate around center
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);
        at.rotate(rads, w / 2.0, h / 2.0);

        // Apply transform with NEAREST_NEIGHBOR to keep sharp edges
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(img, rotated);

        g2d.dispose();
        return rotated;
    }
    public static BufferedImage mirrorImage(BufferedImage original, boolean mirrorX, boolean mirrorY) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage mirrored = new BufferedImage(width, height, original.getType());
        Graphics2D g = mirrored.createGraphics();
        AffineTransform transform = AffineTransform.getScaleInstance(1d, 1);
        if (mirrorX) {
            transform.scale(-1, 1);
            transform.translate(-width, 0);
        }
        if (mirrorY) {
            transform.scale(1, -1);
            transform.translate(0, -height);
        }
        g.drawImage(original, transform, null);
        g.dispose();
        return mirrored;
    }
    public static Map<Color, Color> imageToColorMap(BufferedImage img, int fromRow, int toRow) {
        Map<Color, Color> colorMap = new HashMap<Color, Color>();
        for (int x = 0; x < img.getWidth(); x++) {
            colorMap.put(new Color(img.getRGB(x, fromRow)), new Color(img.getRGB(x, toRow)));
        }
        return colorMap;
    }

    public static BufferedImage rotateImageBad(BufferedImage image, double angleDegrees) {
        double angleRad = Math.toRadians(angleDegrees);
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // Calculate the new bounding box size after rotation
        AffineTransform rotation = AffineTransform.getRotateInstance(angleRad, width / 2.0, height / 2.0);
        Rectangle bounds = new AffineTransformOp(rotation, AffineTransformOp.TYPE_BILINEAR).getBounds2D(image).getBounds();

        // Create a new image with size to fit the rotated bounds
        BufferedImage rotatedImage = new BufferedImage(bounds.width, bounds.height, image.getType());

        Graphics2D g2d = rotatedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Translate to center the original image in the new bounds
        double tx = (bounds.width - width) / 2.0;
        double ty = (bounds.height - height) / 2.0;
        g2d.translate(tx, ty);

        // Rotate around the center of the original image
        g2d.rotate(angleRad, width / 2.0, height / 2.0);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return rotatedImage;
    }
    public static BufferedImage getRandomatedImage(BufferedImage originalImage, Point pixelatedSize, Point realSize, Double pixelOrTransperentChance, int lowestAlpha) {
        if (originalImage == null || pixelatedSize == null || realSize == null || pixelOrTransperentChance == null) {return null;}
        BufferedImage returnImage = new BufferedImage(pixelatedSize.x, pixelatedSize.y, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < returnImage.getWidth(); i++) {
            for (int ii = 0; ii < returnImage.getHeight(); ii++) {
                if (random.nextDouble() <= pixelOrTransperentChance) {
                    Integer color = null;
                    int tries = 0;
                    while (color == null || new Color(color, true).getAlpha() < lowestAlpha) {
                        color = originalImage.getRGB(random.nextInt(originalImage.getWidth()), random.nextInt(originalImage.getHeight()));
                        tries++;
                        if (tries > 200) {
                            System.out.println("Maxed out tries to find a not clear pixel in PointCalculator.getRandomatedImage");
                            break;
                        }
                    }
                    if (color != null) {
                        returnImage.setRGB(i, ii, color);
                    }
                }
            }
        }
        return resizeImage(returnImage, realSize.x, realSize.y);
    }

    public static String swapLetters(String string, Map<String, String> letters) {
        for (Map.Entry<String, String> entry : letters.entrySet()) {
            String[] splitString = string.split(entry.getKey());
            string = String.join(entry.getValue(), splitString);
        }
        return string;
    }
    public static ArrayList<String> stringToLines(String text, int maxWidth, FontMetrics fm) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        
        for (String word : text.split(" ")) {
            String testLine = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(testLine) <= maxWidth || line.isEmpty()) {
                line = new StringBuilder(testLine);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
    public static Rectangle getLinesPerimeterRect(ArrayList<String> lines, Point center, int offsetType, Point offset, FontMetrics fm) {
        offset = new Point(offset);  //so I can change the x and y values later without changing wherever this Point came from
        if (lines.size() == 0) {
            System.out.println("Lines is empty in PointCalculator.getLinesPerimeterRect()");
            printStackTrace();
            return null;
        }
        int lineWidth = 0;
        int fontHeight = fm.getHeight();
        for (String line : lines) {
            if (fm.stringWidth(line) > lineWidth) {
                lineWidth = fm.stringWidth(line);
            }
        }
        Rectangle basicRect = new Rectangle(0, 0, lineWidth, fontHeight * lines.size());
        Point.Double rectOffsetMultiplier = new Point.Double(0, 0);
        /*offset types:
        0-nothing
        1-Center
        2-top
        3-top right
        4-right
        5-bottom right
        6-bottom
        7-bottom left
        8-left
        9-top left

        9  2  3
        8  1  4
        7  6  5
        */
        switch (offsetType) {
            case 0:
                //do nothing
                break;
            case 1:
                rectOffsetMultiplier = new Point.Double(0, 0);
                offset.x *= 0;
                offset.y *= 0;
                break;
            case 2:
                rectOffsetMultiplier = new Point.Double(0, -0.5);
                offset.x *= 0;
                offset.y *= -1;
                break;
            case 3:
                rectOffsetMultiplier = new Point.Double(0.5, -0.5);
                offset.y *= -1;
                break;
            case 4:
                rectOffsetMultiplier = new Point.Double(0.5, 0);
                offset.y *= 0;
                break;
            case 5:
                rectOffsetMultiplier = new Point.Double(0.5, 0.5);
                break;
            case 6 :
                rectOffsetMultiplier = new Point.Double(0, 0.5);
                offset.x *= 0;
                break;
            case 7:
                rectOffsetMultiplier = new Point.Double(-0.5, 0.5);
                offset.x *= -1;
                break;
            case 8:
                rectOffsetMultiplier = new Point.Double(-0.5, 0);
                offset.x *= -1;
                offset.y *= 0;
                break;
            case 9:
                rectOffsetMultiplier = new Point.Double(-0.5, -0.5);
                offset.x *= -1;
                offset.y *= -1;
                break;
        }
        Point newCenter = new Point((int)(center.x + (lineWidth * rectOffsetMultiplier.x) + offset.x), (int)(center.y + (basicRect.height * rectOffsetMultiplier.y) + offset.y));
        Rectangle returnRect = new Rectangle(newCenter.x - basicRect.width / 2, newCenter.y - basicRect.height / 2, basicRect.width, basicRect.height);
        return returnRect;
    }
    public static Rectangle getStringSize(String string, FontMetrics fm, boolean onlyAssent) {
        if (string == null || string.equals("")) {
            return new Rectangle(0, 0, 0, 0);
        } else if (fm == null) {
            System.out.println("fm is null in PointCalculator.getStringSize()!");
            printStackTrace();
            return null;
        }
        int x = fm.stringWidth(string);
        int y = fm.getAscent() + (onlyAssent ? 0 : fm.getLeading());
        return new Rectangle(0, 0, x, y);
    }
    public static String setStringLength(boolean ignorDash, int maxCharacters, String string) {
        String dash = "";
        if (ignorDash) {
            if (string.startsWith("-")) {
                dash = "-";
                string = string.substring(1, string.length());
            }
        }
        String spaces = "";
        if (string.length() > maxCharacters) {
            string = string.substring(0, maxCharacters);
        } else if (string.length() < maxCharacters) {
            for (int i = maxCharacters - string.length(); i > 0; i--) {
                spaces += " ";
            }
        }
        return dash + string + spaces;
    }
    public static String trimString(boolean ignorDash, int maxCharacters, String string) {
        String dash = "";
        if (ignorDash) {
            if (string.startsWith("-")) {
                dash = "-";
                string = string.substring(1, string.length());
            }
        }
        if (string.length() > maxCharacters) {
            string = string.substring(0, maxCharacters);
        }
        return dash + string;
    }
    public static String toString(Rectangle r) {
        if (r == null) {return "null";}
        return "Rectangle[x=" + r.x + ", y=" + r.y + ", width=" + r.width + ", height=" + r.height + "]";
    }
    public static String toString(Point p) {
        if (p == null) {return "null";}
        return "Point[x=" + p.x + ", y=" + p.y + "]";
    }
    public static String toString(Integer integer) {
        if (integer == null) {
            return "null";
        } else {
            return integer.toString();
        }
    }
    public static <T> void addArrayListToArrayList(ArrayList<T> originalList, ArrayList<T> additionalList) {
        for(T t : additionalList) {
            originalList.add(t);
        }
    }

    // Escape Character Stuff (\)
    public static boolean hasSpecialCodes(String str) {
        if (str == null) return false;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            // Checks for literal backslash OR non-printable control characters
            if (ch == '\\' || ch < 32 || ch == 127) {
                return true; 
            }
        }
        return false;
    }

    public static <T extends Enum<T>> void printEnumValues(Class<T> enumClass) {
        // Retrieves an array of all constants in this enum
        T[] constants = enumClass.getEnumConstants();
        
        if (constants != null) {
            for (T value : constants) {
                System.out.println(value);
            }
        }
    }
    public static <C extends Enum<C>> Integer enumToInteger(Class<C> enumClass, Enum<?> value) {
        // Retrieves an array of all constants in this enum
        C[] constants = enumClass.getEnumConstants();
        
        if (constants != null) {
            int i = 0;
            for (C type : constants) {
                if (value == type) {
                    return i;
                }
                i++;
            }
        }
        return null;
    }
    public static <C extends Enum<C>> Short enumToShort(Class<C> enumClass, Enum<?> value) {
        // Retrieves an array of all constants in this enum
        C[] constants = enumClass.getEnumConstants();
        
        if (constants != null) {
            short i = 0;
            for (C type : constants) {
                if (value == type) {
                    return i;
                }
                i++;
            }
        }
        return null;
    }
    public static <C extends Enum<C>> Byte enumToByte(Class<C> enumClass, Enum<?> value) {
        // Retrieves an array of all constants in this enum
        C[] constants = enumClass.getEnumConstants();
        
        if (constants != null) {
            byte i = 0;
            for (C type : constants) {
                if (value == type) {
                    return i;
                }
                i++;
            }
        }
        return null;
    }
    public static PacketDataType byteToPacketDataType(byte enumIndex) {
        // Retrieves an array of all constants in this enum
        byte i = 0;
        for (PacketDataType dataType : PacketDataType.values()) {
            if (i == enumIndex) {return dataType;}
            i++;
        }
        System.out.println("Could not find PacketDataType that matched index " + enumIndex + " in EngineCalculator.integerToPacketDataType()!");
        printStackTrace();
        return null;
    }
    public static PacketPurpose shortToPacketPurpose(short enumIndex) {
        // Retrieves an array of all constants in this enum
        short i = 0;
        for (PacketPurpose purpose : PacketPurpose.values()) {
            if (i == enumIndex) {return purpose;}
            i++;
        }
        System.out.println("Could not find PacketPurpose that matched index " + enumIndex + " in EngineCalculator.integerToPacketPurpose()!");
        printStackTrace();
        return null;
    }

    // System Clipboard Stuff
    public static void setClipboardText(String text) {
        if (text == null) {return;}
        StringSelection selection = new StringSelection(text);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }
    public static String getClipboardText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                // Fetch the data and cast it back to a standard Java String
                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            // Handles cases where the clipboard is busy, locked, or inaccessible
            System.err.println("Failed to read clipboard: " + e.getMessage());
        }
        return "";
    }


    // Universal Tween Engine Stuff
    public static double tweenValue(double progression, TweenEquation tweenType) {
        if (tweenType == null) {tweenType = TweenEquations.easeOutCubic;}
        return tweenType.compute((float)(progression));

        /* Tween Options:
        Best: easeOutCubic (0.45 sec)
        
        Other Options:
        easeInOutSine
        easeInOutQuad
        easeOutSine
        easeOutCirc
        
        Fun Options:
        easeInOutBack (1 sec)
        easeInOutBounce
        */
    }
    public static double tweenValues(int start, int end, double progression, TweenEquation tweenType) {
        return (double) start + (end - start) * tweenValue(progression, tweenType);
    }
    public static double tweenValues(double start, double end, double progression, TweenEquation tweenType) {
        return (double) start + (end - start) * tweenValue(progression, tweenType);
    }
    
    
    //somewhat random or uses objects/classes that don't exist in this project
    /*
    
    public static int getSliderProgressX(int segments, Slider s) {
        Rectangle sliderRect = new Rectangle(s.sliderCore.sliderRect);
        int position = sliderRect.x + sliderRect.width / 2;
        Rectangle slideRange = s.sliderCore.slideRange;
        Point range = new Point(slideRange.x + sliderRect.width / 2, slideRange.x + slideRange.width - sliderRect.width / 2);
        Double segmentSize = 1.0 * (range.y - range.x) / segments;
        for (int i = 1; i <= segments; i++) {
            if (i == segments || (position >= segmentSize * (i - 1) && position <= segmentSize * i)) {
                return i;
            }
        }
        System.out.println("The Thread should not have gotten here.");
        printStackTrace();
        //quit game
        return 0;
    }
    public static int getSliderProgressY(int segments, Slider s) {
        Rectangle sliderRect = new Rectangle(s.getSliderRect());
        int position = sliderRect.y + sliderRect.height / 2;
        Rectangle slideRange = s.getSlideRange();
        Point range = new Point(slideRange.y + sliderRect.height / 2, slideRange.y + slideRange.height - sliderRect.height / 2);
        int segmentSize = (range.x - range.y) / segments;
        for (int i = 1; i <= segments; i++) {
            if (i == segments || (position >= segmentSize * (i - 1) && position < segmentSize * i)) {
                return i;
            }
        }
        System.out.println("The Thread should not have gotten here.");
        printStackTrace();
        //quit game
        return 0;
    }
    public static ArrayList<ImagePointColor> getRandomPixelsFromImage(BufferedImage originalImage, int deletePixelsQuantity, int lowestDeleteAlpha) {
        BufferedImage img = getImageCopy(originalImage);
        ArrayList<ImagePointColor> list = new ArrayList<>();
        for (int i = 0; i < deletePixelsQuantity; i++) {
            if (isImageEmpty(img, 0)) {break;}
            int x = randomInt(img.getWidth());
            int y = randomInt(img.getHeight());
            int alpha = (img.getRGB(x, y) >> 24) & 0xff;
            if (alpha < lowestDeleteAlpha) {
                i--;
                continue;
            } else {
                Color color = new Color(img.getRGB(x, y));
                Point point = new Point(x, y);
                list.add(new ImagePointColor(null, point, color));
                img.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
            }
        }
        if (list.size() == 0) {
            list.add(new ImagePointColor(img, null, null));
        } else {
            list.get(0).image = img;
        }
        return list;
    }
    */


    //very important
    /*
    //origin is in relation to the image so 0, 0 would be the position of the image
    public static ImagePoint rotateImageAroundPoint(BufferedImage image, Dimension imageSize, Point originPosition, Point originImagePosition, double rotation) {
        if (image.getWidth() != imageSize.width || image.getHeight() != imageSize.height) {
            image = resizeImage(image, imageSize.width, imageSize.height);
        }
        //create variables
        Point corner = new Point(originPosition.x - originImagePosition.x, originPosition.y - originImagePosition.y);
        Point centerOffset = new Point(imageSize.width / 2, imageSize.height / 2);
        Point center = new Point(corner.x + centerOffset.x, corner.y + centerOffset.y);
        Point pivot = new Point(originPosition.x, originPosition.y);

        //rotate point
        Point rotatedPivot = rotatePoint(center, pivot, rotation);
        Point rotatedPivotToCenter = new Point(center.x - rotatedPivot.x, center.y - rotatedPivot.y);

        //rotate image
        BufferedImage rotatedImage = rotateImage(image, rotation);
        Dimension newImageSize = new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());

        //finish up
        Point sizeDifferenceOffset = new Point((newImageSize.width - imageSize.width) / 2, (newImageSize.height - imageSize.height) / 2);
        Point newCorner = new Point(corner.x + rotatedPivotToCenter.x - sizeDifferenceOffset.x, corner.y + rotatedPivotToCenter.y - sizeDifferenceOffset.y);

        Point centerToPivot = new Point(center.x - pivot.x, center.y - pivot.y);
        Point returnCorner = new Point(newCorner.x - centerToPivot.x, newCorner.y - centerToPivot.y);

        return new ImagePoint(rotatedImage, returnCorner);
    }
        
    //with this method you can use a double to say what amount of the image the origin should be through......that dosen't make any sense does it.........
    public static ImagePoint rotateImageAroundPoint(BufferedImage image, Dimension imageSize, Point originPosition, Point.Double originImageDouble, double rotation) {
        if (image.getWidth() != imageSize.width || image.getHeight() != imageSize.height) {
            resizeImage(image, imageSize.width, imageSize.height);
        }
        Point originImagePosition = new Point((int)(imageSize.width * originImageDouble.x), (int)(imageSize.height * originImageDouble.y));
        //create variables
        Point corner = new Point(originPosition.x - originImagePosition.x, originPosition.y - originImagePosition.y);
        Point centerOffset = new Point(imageSize.width / 2, imageSize.height / 2);
        Point center = new Point(corner.x + centerOffset.x, corner.y + centerOffset.y);
        Point pivot = new Point(originPosition.x, originPosition.y);

        //rotate point
        Point rotatedPivot = rotatePoint(center, pivot, rotation);
        Point rotatedPivotToCenter = new Point(center.x - rotatedPivot.x, center.y - rotatedPivot.y);

        //rotate image
        BufferedImage rotatedImage = rotateImage(image, rotation);
        Dimension newImageSize = new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());

        //finish up
        Point sizeDifferenceOffset = new Point((newImageSize.width - imageSize.width) / 2, (newImageSize.height - imageSize.height) / 2);
        Point newCorner = new Point(corner.x + rotatedPivotToCenter.x - sizeDifferenceOffset.x, corner.y + rotatedPivotToCenter.y - sizeDifferenceOffset.y);

        Point centerToPivot = new Point(center.x - pivot.x, center.y - pivot.y);
        Point returnCorner = new Point(newCorner.x - centerToPivot.x, newCorner.y - centerToPivot.y);

        return new ImagePoint(rotatedImage, returnCorner);
    }
    //origin is in relation to the image so 0, 0 would be the position of the image
    public static ImagePoint rotateImageAroundPoint(BufferedImage image, Point.Double imageSizeMultiplier, Point originPosition, Point originImagePosition, double rotation) {
        Dimension imageSize = new Dimension((int)(image.getWidth() * imageSizeMultiplier.x), (int)(image.getHeight() * imageSizeMultiplier.y));
        if (image.getWidth() != imageSize.width || image.getHeight() != imageSize.height) {
            resizeImage(image, imageSize.width, imageSize.height);
        }
        //create variables
        Point corner = new Point(originPosition.x - originImagePosition.x, originPosition.y - originImagePosition.y);
        Point centerOffset = new Point(imageSize.width / 2, imageSize.height / 2);
        Point center = new Point(corner.x + centerOffset.x, corner.y + centerOffset.y);
        Point pivot = new Point(originPosition.x, originPosition.y);

        //rotate point
        Point rotatedPivot = rotatePoint(center, pivot, rotation);
        Point rotatedPivotToCenter = new Point(center.x - rotatedPivot.x, center.y - rotatedPivot.y);

        //rotate image
        BufferedImage rotatedImage = rotateImage(image, rotation);
        Dimension newImageSize = new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());

        //finish up
        Point sizeDifferenceOffset = new Point((newImageSize.width - imageSize.width) / 2, (newImageSize.height - imageSize.height) / 2);
        Point newCorner = new Point(corner.x + rotatedPivotToCenter.x - sizeDifferenceOffset.x, corner.y + rotatedPivotToCenter.y - sizeDifferenceOffset.y);

        Point centerToPivot = new Point(center.x - pivot.x, center.y - pivot.y);
        Point returnCorner = new Point(newCorner.x - centerToPivot.x, newCorner.y - centerToPivot.y);

        return new ImagePoint(rotatedImage, returnCorner);
    }
    //with this method you can use a double to say what amount of the image the origin should be through......that dosen't make any sense does it.........
    public static ImagePoint rotateImageAroundPoint(BufferedImage image, Point.Double imageSizeMultiplier, Point originPosition, Point.Double originImageDouble, double rotation) {
        Dimension imageSize = new Dimension((int)(image.getWidth() * imageSizeMultiplier.x), (int)(image.getHeight() * imageSizeMultiplier.y));
        if (image.getWidth() != imageSize.width || image.getHeight() != imageSize.height) {
            resizeImage(image, imageSize.width, imageSize.height);
        }
        Point originImagePosition = new Point((int)(imageSize.width * originImageDouble.x), (int)(imageSize.height * originImageDouble.y));
        //create variables
        Point corner = new Point(originPosition.x - originImagePosition.x, originPosition.y - originImagePosition.y);
        Point centerOffset = new Point(imageSize.width / 2, imageSize.height / 2);
        Point center = new Point(corner.x + centerOffset.x, corner.y + centerOffset.y);
        Point pivot = new Point(originPosition.x, originPosition.y);

        //rotate point
        Point rotatedPivot = rotatePoint(center, pivot, rotation);
        Point rotatedPivotToCenter = new Point(center.x - rotatedPivot.x, center.y - rotatedPivot.y);

        //rotate image
        BufferedImage rotatedImage = rotateImage(image, rotation);
        Dimension newImageSize = new Dimension(rotatedImage.getWidth(), rotatedImage.getHeight());

        //finish up
        Point sizeDifferenceOffset = new Point((newImageSize.width - imageSize.width) / 2, (newImageSize.height - imageSize.height) / 2);
        Point newCorner = new Point(corner.x + rotatedPivotToCenter.x - sizeDifferenceOffset.x, corner.y + rotatedPivotToCenter.y - sizeDifferenceOffset.y);

        Point centerToPivot = new Point(center.x - pivot.x, center.y - pivot.y);
        Point returnCorner = new Point(newCorner.x - centerToPivot.x, newCorner.y - centerToPivot.y);

        return new ImagePoint(rotatedImage, returnCorner);
    }
    */
}
