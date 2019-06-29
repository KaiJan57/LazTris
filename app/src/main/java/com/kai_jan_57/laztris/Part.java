package com.kai_jan_57.laztris;

import android.graphics.Color;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Part {
    public static class Initializer {
        Initializer(char[][] pointDefinition, int yOffset, RotationType rotation, int color, int probability) {
            this.pointDefinition = pointDefinition;
            this.yOffset = yOffset;
            this.rotation = rotation;
            this.color = color;
            this.probability = probability;
        }
        char[][] pointDefinition;
        int yOffset;
        RotationType rotation;
        int color;
        int probability;
    }

    public static class Manager {
        private static final Initializer[] partTypes = {
                new Initializer(
                        new char[][] {
                            {'#', '#'},
                            {'#', '#'},
                        },
                        0,
                        RotationType.NONE,
                        Color.rgb(255, 255, 0),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#', '#'},
                        },
                        1,
                        RotationType.TWO_STATE,
                        Color.rgb(0, 255, 255),
                        5
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#'},
                            {' ', '#', ' '},
                        },
                        1,
                        RotationType.FULL,
                        Color.rgb(255, 0, 255),
                        4
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#'},
                            {'#', ' ', ' '},
                        },
                        1,
                        RotationType.FULL,
                        Color.rgb(200, 200, 0),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#'},
                            {' ', ' ', '#'},
                        },
                        1,
                        RotationType.FULL,
                        Color.rgb(0, 100, 200),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', ' '},
                            {' ', '#', '#'},
                        },
                        1,
                        RotationType.TWO_STATE,
                        Color.rgb(255, 0, 0),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {' ', '#', '#'},
                            {'#', '#', ' '},
                        },
                        1,
                        RotationType.TWO_STATE,
                        Color.rgb(0, 255, 0),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {'#'},
                        },
                        0,
                        RotationType.NONE,
                        Color.rgb(0, 200, 0),
                        3
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#'},
                            {'#', ' ', '#'},
                            {'#', ' ', '#'},
                        },
                        0,
                        RotationType.FULL,
                        Color.rgb(0, 200, 200),
                        2
                ),
                new Initializer(
                        new char[][] {
                            {'#', '#', '#'},
                            {'#', ' ', '#'},
                            {'#', '#', '#'},
                        },
                        0,
                        RotationType.NONE,
                        Color.rgb(200, 0, 0),
                        1
                ),
        };

        public static int getPartCount() {
            return partTypes.length;
        }

        public static Part getPart(int id) {
            if (partTypes.length < 1)
            {
                throw new RuntimeException("No parts defined!");
            }
            if (id < 0)
            {
                id = 0;
            }
            else if (id > partTypes.length - 1) {
                id = partTypes.length - 1;
            }
            return new Part(partTypes[id].pointDefinition, partTypes[id].yOffset, partTypes[id].rotation, partTypes[id].color);
        }

        private static Random random = new Random();
        static Part getRandomPart() {
            int maxRandom = 0;
            for (Initializer init : partTypes) {
                maxRandom += init.probability;
            }
            int randomValue = random.nextInt(maxRandom);
            int previous = 0;
            for (int i = 0; i < partTypes.length; i++) {
                if ((randomValue >= previous) && (randomValue < (previous += partTypes[i].probability))) {
                    return getPart(i);
                }
            }
            throw new RuntimeException("Random part picker not working correctly. Please check Part.java");
        }
    }

    private Part(char[][] pointDefinition, int yOffset, RotationType rotationType, int color) {
        this.yOffset = yOffset;
        this.points = convertPointDefinition(pointDefinition);
        this.color = color;
        this.rotationType = rotationType;
    }

    private Point[] convertPointDefinition(char[][] pointDefinition) {
        List<Point> points = new ArrayList<>();
        for (int y = 0; y < pointDefinition.length; y++) {
            for (int x = 0; x < pointDefinition[0].length; x++) {
                if (pointDefinition[y][x] != ' ') {
                    points.add(new Point(x, y));
                }
            }
        }

        /*int i = 0;
        for (Point p : points) {
            if (i == 0) {
                yOffset = p.y;
            }
            else if (p.y < yOffset) {
                yOffset = p.y;
            }
            i++;
        }
        yOffset *= -1;*/
        boxWidth = width = pointDefinition[0].length;
        height = pointDefinition.length;
        boxHeight = height + yOffset;
        Point[] result = points.toArray(new Point[0]);
        if (yOffset != 0) {
            for (int i = 0; i < points.size(); i++) {
                result[i].y += yOffset;
            }
        }
        return points.toArray(new Point[0]);
    }

    private Point[] points;
    public Point[] getPoints() {
        return addPosition(points, boxWidth);
    }

    Point position = new Point(0, 0);

    private int yOffset;


    public int color;

    private int width;
    public int getWidth() {
        return width;
    }

    private int height;
    public int getHeight() {
        return height;
    }

    private int boxWidth;
    private int boxHeight;

    private RotationType rotationType;

    private boolean directionToggle = false;

    private Point[] addPosition(Point[] points, int width) {
        Point[] positioned = new Point[points.length];
        for (int i = 0; i < positioned.length; i++) {
            positioned[i] = new Point(points[i].x - width/2 + position.x, points[i].y + position.y - yOffset);
        }
        return positioned;
    }

    public enum RotationDirection {
        RIGHT,
        LEFT,
    }

    public enum RotationType {
        NONE,
        TWO_STATE,
        FULL,
    }

    @SuppressWarnings("SuspiciousNameCombination")
    void rotate(RotationDirection direction) {
        if (rotationType == RotationType.NONE) {
            return;
        }
        else if (rotationType == RotationType.TWO_STATE) {
            if (directionToggle) {
                direction = RotationDirection.LEFT;
            }
            else {
                direction = RotationDirection.RIGHT;
            }
            directionToggle = !directionToggle;
        }

        int temp = width;
        width = height;
        height = temp;

        temp = boxWidth;
        boxWidth = boxHeight;
        boxHeight = temp;

        for (Point point : points) {
            temp = point.x;
            point.x = point.y;
            point.y = temp;

            if (direction == RotationDirection.RIGHT) {
                point.x = boxWidth - 1 - point.x;
            } else {
                point.y = boxHeight - 1 - point.y;
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    Point[] rotatePreview(RotationDirection direction) {
        if (rotationType == RotationType.NONE) {
            return points;
        }
        else if (rotationType == RotationType.TWO_STATE) {
            if (directionToggle) {
                direction = RotationDirection.LEFT;
            }
            else {
                direction = RotationDirection.RIGHT;
            }
        }

        int boxWidthPreview = boxHeight;
        int boxHeightPreview = boxWidth;

        Point[] pointsPreview = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            pointsPreview[i] = new Point(points[i].x, points[i].y);
        }

        int temp;
        for (Point aPointsPreview : pointsPreview) {
            temp = aPointsPreview.x;
            aPointsPreview.x = aPointsPreview.y;
            aPointsPreview.y = temp;

            if (direction == RotationDirection.RIGHT) {
                aPointsPreview.x = boxWidthPreview - 1 - aPointsPreview.x;
            } else {
                aPointsPreview.y = boxHeightPreview - 1 - aPointsPreview.y;
            }
        }
        return addPosition(pointsPreview, boxWidthPreview);
    }

    void move(int amount) {
        position.x += amount;
    }

    Point[] movePreview(int amount) {
        Point[] pointsPreview = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            pointsPreview[i] = new Point(points[i].x, points[i].y);
        }

        for (Point aPointsPreview : pointsPreview) {
            aPointsPreview.x += amount;
        }
        return addPosition(pointsPreview, boxWidth);
    }

    void fall(int amount) {
        position.y += amount;
    }

    Point[] fallPreview(int amount) {
        Point[] pointsPreview = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            pointsPreview[i] = new Point(points[i].x, points[i].y + amount);
        }
        return addPosition(pointsPreview, boxWidth);
    }
}
