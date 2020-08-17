package com.javarush.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    public int score;
    public int maxTile;
    private Stack previousStates;
    private Stack previousScores;
    private boolean isSaveNeeded = true;


    public Model() {
        score = 0;
        maxTile = 0;
        previousStates = new Stack();
        previousScores = new Stack();
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> list = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty())
                    list.add(gameTiles[i][j]);
            }
        }
        return list;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() != 0) {
            int randomObjectIndex = (int) (emptyTiles.size() * Math.random());
            Tile tile = emptyTiles.get(randomObjectIndex);

            for (int i = 0; i < FIELD_WIDTH; i++) {
                for (int j = 0; j < FIELD_WIDTH; j++) {
                    if (gameTiles[i][j] == tile)
                        gameTiles[i][j].value = (Math.random() < 0.9 ? 2 : 4);
                }
            }
        }
    }

    private boolean compressTiles(Tile[] tiles) {
        Tile[] before = tiles.clone();
        for (int j = 0; j < tiles.length; j++) {
            for (int i = 0; i < tiles.length; i++) {
                if (tiles[i].isEmpty() && i != tiles.length - 1) {
                    Tile copy = tiles[i];
                    tiles[i] = tiles[i + 1];
                    tiles[i + 1] = copy;
                }
            }
        }
        return !Arrays.equals(before, tiles);
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean toReturn = false;
        for (int i = 1; i < tiles.length; i++) {
            if (tiles[i].equals(tiles[i - 1]) && !tiles[i].isEmpty()) {
                int newValue = tiles[i].value + tiles[i - 1].value;

                if (newValue > maxTile)
                    maxTile = newValue;

                score += newValue;

                tiles[i - 1].value = newValue;
                tiles[i].value = 0;
                toReturn = true;
            }
        }
        compressTiles(tiles);
        return toReturn;
    }

    public void left() {
        if (isSaveNeeded)
            saveState(gameTiles);

        boolean compressedOrMerged = false;
        for (int i = 0; i < gameTiles.length; i++) {
            boolean isCompressed = compressTiles(gameTiles[i]);
            boolean isMerged = mergeTiles(gameTiles[i]);

            if (isCompressed || isMerged)
                compressedOrMerged = true;
        }

        if (compressedOrMerged)
            addTile();

         isSaveNeeded = true;
    }

    public void rotate(Tile[][] tiles) {
        int n = tiles.length;

        for (int i = 0; i < n / 2; i++) {
            for (int j = i; j < n - i - 1; j++) {
                Tile tile = tiles[i][j];
                tiles[i][j] = tiles[j][n - 1 - i];
                tiles[j][n - 1 - i]
                        = tiles[n - 1 - i][n - 1 - j];

                tiles[n - 1 - i][n - 1 - j] = tiles[n - 1 - j][i];
                tiles[n - 1 - j][i] = tile;
            }
        }
    }

    public void up() {
        saveState(gameTiles);

        rotate(gameTiles);
        left();
        for (int i = 0; i < 3; i++) {
            rotate(gameTiles);
        }
    }

    public void right() {
        saveState(gameTiles);

        for (int i = 0; i < 2; i++) {
            rotate(gameTiles);
        }
        left();
        for (int i = 0; i < 2; i++) {
            rotate(gameTiles);
        }
    }

    public void down() {
        saveState(gameTiles);

        for (int i = 0; i < 3; i++) {
            rotate(gameTiles);
        }
        left();
        rotate(gameTiles);

    }

    public boolean canMove() {
        boolean canMove = false;
        boolean hasEmptyTiles = getEmptyTiles().size() > 0;


        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 1; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].equals(gameTiles[i][j - 1]))
                    canMove = true;
            }
        }
        for (int i = 1; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[0].length; j++) {
                if (gameTiles[i][j].equals(gameTiles[i-1][j]))
                    canMove = true;
            }
        }
        return (canMove || hasEmptyTiles);
    }

    private void saveState (Tile[][] tiles) {
        Tile[][] copy = new Tile[tiles.length][tiles[0].length];
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                copy[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(copy);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousScores.empty() && !previousStates.empty()) {
        gameTiles = (Tile[][]) previousStates.pop();
        score = (int) previousScores.pop(); }
    }

    public void randomMove() {
        int number = ((int) (Math.random() * 100)) % 4;
        switch (number) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean hasBoardChanged() {
        int previousValue = 0;
        int currentValue = 0;
       Tile[][] previous = (Tile[][]) previousStates.peek();

       for (int i = 0; i < previous.length; i++) {
           for (int j = 0; j < previous[i].length; j++) {
               previousValue += previous[i][j].value;
               currentValue += gameTiles[i][j].value;
           }
       }

       if (previousValue != currentValue)
           return true;
       else return false;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency toReturn = null;
        move.move();

        if (!hasBoardChanged()) {
           toReturn =  new MoveEfficiency(-1, 0, move);
        }
        else {
            int newNumberOfEmptyTiles = getEmptyTiles().size();
            int newScore = score;
            toReturn = new MoveEfficiency(newNumberOfEmptyTiles, newScore, move);
        }

        rollback();
        return toReturn;
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue =
                new PriorityQueue<MoveEfficiency>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(() -> left()));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::up));
        queue.peek().getMove().move();

    }
}
