package net.xakep.game;

import java.util.concurrent.RecursiveTask;

public class CalculateScoreTask extends RecursiveTask<Integer> {
    private Boolean[] table;
    private boolean player;
    private int position;

    public CalculateScoreTask(Boolean[] table, boolean player, int position) {
        this.table = table;
        this.player = player;
        this.position = position;
    }

    @Override
    protected Integer compute() {
        Boolean[] tableCopy = new Boolean[table.length];
        //Копируем начальное положение, чтобы не попортить
        System.arraycopy(table, 0, tableCopy, 0, tableCopy.length);

        //Меняем вражеские фишки на свои
        CalculateUtils.apply(tableCopy, position, player, true);

		//Подсчитываем результат
        return CalculateUtils.score(tableCopy, player);
    }

    public int getPosition() {
        return position;
    }
}
