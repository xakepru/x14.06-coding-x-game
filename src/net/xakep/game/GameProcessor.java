package net.xakep.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class GameProcessor {
    public GameProcessor() {
    }

    public int nextMove(final Boolean[] table, final Boolean player) {
        RecursiveTask<Integer> task = new RecursiveTask<Integer>() {
            @Override
            protected Integer compute() {
                List<CalculateScoreTask> tasks = new ArrayList<CalculateScoreTask>(table.length);

                //Поочередно ставим фишку в каждую незанятую клетку
                //и считаем, сколько фишек при этом получим.
                for (int position = 0; position < table.length; position++) {
                    if (table[position] == null) {

                        //создаем задачу, которая считает результат для одной позиции
                        CalculateScoreTask task = new CalculateScoreTask(table, player, position) {
                            @Override
                            protected Integer compute() {
                                int maxScore = super.compute();

                                //считаем максимум, который получит враг
                                List<RecursiveTask<Integer>> tasks = new ArrayList<RecursiveTask<Integer>>(table.length);
                                for (int position = 0; position < table.length; position++) {
                                    if (table[position] == null) {
                                        CalculateScoreTask task = new CalculateScoreTask(table, !player, position);
                                        task.fork();
                                        tasks.add(task);
                                    }
                                }

                                int maxEnemyScore = 0;
                                for (RecursiveTask<Integer> task : tasks) {
                                    Integer result = task.join();
                                    if (result > maxEnemyScore) {
                                        maxEnemyScore = result;
                                    }
                                }

                                return maxScore - maxEnemyScore;
                            }
                        };
                        task.fork();
                        tasks.add(task);
                    }
                }

                int position = -1;
                int newMaxScore = Integer.MIN_VALUE;
                for (CalculateScoreTask task : tasks) {
                    Integer result = task.join();
                    if (result > newMaxScore) {
                        newMaxScore = result;
                        position = task.getPosition();
                    }
                }

                return position;
            }
        };

        new ForkJoinPool().invoke(task);
        Integer result = task.join();
        return result;
    }

    public boolean isGameOver(Boolean[] table, boolean player) {
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null && table[i] == player) {
                CalculateUtils.apply(table, i, table[i], false);
            }
        }

        //проверим, есть ли еще свободные ячейки поля
        for (int i = 0; i < table.length; i++) {
            if (table[i] == null) {
                return false;
            }
        }
        return true;
    }
}
