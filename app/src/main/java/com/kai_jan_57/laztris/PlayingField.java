package com.kai_jan_57.laztris;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class PlayingField {
    public static class ColorPoint {
        public ColorPoint(Point point, int color) {
            this.point = point;
            this.color = color;
        }
        public Point point;
        public int color;
    }

    public PlayingField(int width, int height) {
        this.width = width;
        this.height = height;
        partCurrent = Part.Manager.getRandomPart();
        partNext = Part.Manager.getRandomPart();
        partCurrent.position = new Point(width/2, 0);
    }

    public int width;
    public int height;

    private List<ColorPoint> gridContent = new ArrayList<>();
    public List<ColorPoint> getGridContent() {
        return gridContent;
    }

    private Part partCurrent, partNext;
    public Part getPartCurrent() {
        return partCurrent;
    }
    public Part getPartNext() {
        return partNext;
    }

    public interface LostEvent {
        void onLose();
    }

    public interface LineCompletedEvent {
        void onLineCompleted(int amount);
    }

    private LostEvent lostEventHandler;
    private LineCompletedEvent lineCompletedEventHandler;

    public void registerLostEvent(LostEvent lostEvent) {
        lostEventHandler = lostEvent;
    }

    public void registerLineCompletedEvent(LineCompletedEvent lineCompletedEvent) {
        lineCompletedEventHandler = lineCompletedEvent;
    }

    public void tick() {
        if (canFall(1)) {
            partCurrent.fall(1);
        }
        else {
            for (Point point : partCurrent.getPoints()) {
                gridContent.add(new ColorPoint(point, getPartCurrent().color));
            }

            int linesCompleted = 0;
            for (int y = height - 1; y >= 0;) {
                while (true) {
                    boolean[] checker = new boolean[width];
                    int check = 0;
                    for (int i = 0; i < gridContent.size(); i++) {
                        if (gridContent.get(i).point.y == y) {
                            if (!checker[gridContent.get(i).point.x]) {
                                checker[gridContent.get(i).point.x] = true;
                                check++;
                            }
                        }
                    }
                    if (check == width) {
                        linesCompleted++;
                        for (int i = gridContent.size() - 1; i >= 0; i--) {
                            if (gridContent.get(i).point.y == y) {
                                gridContent.remove(i);
                            }
                        }

                        for (int i = 0; i < gridContent.size(); i++)
                        {
                            if (gridContent.get(i).point.y < y) {
                                gridContent.set(i, new ColorPoint(new Point(gridContent.get(i).point.x, gridContent.get(i).point.y + 1), gridContent.get(i).color));
                            }
                        }
                    }
                    else {
                        y--;
                        break;
                    }
                }
            }
            if (linesCompleted > 0 && lineCompletedEventHandler != null) {
                lineCompletedEventHandler.onLineCompleted(linesCompleted);
            }

            if (checkLost()) {
                if (lostEventHandler != null) {
                    lostEventHandler.onLose();
                }
            }
            else {
                partCurrent = partNext;
                partCurrent.position = new Point(width/2, 0);
                partNext = Part.Manager.getRandomPart();
            }
        }
    }

    private boolean checkCollision(List<ColorPoint> colorPoints, Point point) {
        for (ColorPoint colorPoint : colorPoints) {
            if ((colorPoint.point.x == point.x) && (colorPoint.point.y == point.y)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkLost() {
        Part partTemp = partNext;
        partTemp.position = new Point(width/2, 0);
        for (Point point : partTemp.getPoints()) {
            if (checkCollision(gridContent, point)) {
                return true;
            }
        }
        return false;
    }

    public void move(int amount) {
        if (canMove(amount)) {
            partCurrent.move(amount);
        }
    }

    public void rotate(Part.RotationDirection rotationDirection) {
        if (canRotate(rotationDirection)) {
            partCurrent.rotate(rotationDirection);
        }
    }

    public boolean canFall(int amount) {
        Point[] pointsPreview = partCurrent.fallPreview(amount);
        for (Point point : pointsPreview) {
            if (point.y >= height || checkCollision(gridContent, point)) {
                return false;
            }
        }
        return true;
    }

    private boolean canMove(int amount) {
        Point[] pointsPreview = partCurrent.movePreview(amount);
        for (Point point : pointsPreview) {
            if (point.x < 0 || point.x >= width || checkCollision(gridContent, point)) {
                return false;
            }
        }
        return true;
    }

    private boolean canRotate(Part.RotationDirection rotationDirection) {
        Point[] pointsPreview = partCurrent.rotatePreview(rotationDirection);
        for (Point point : pointsPreview)
        {
            if (point.x < 0 || point.x >= width || point.y >= height || checkCollision(gridContent, point)) {
                return false;
            }
        }
        return true;
    }
}
