package net.xakep.game;

public class CalculateUtils {
    public static void apply(Boolean[] table, int position, Boolean player, boolean ignore) {
        //Если на этой позиции уже наша фишка, то ничего не делаем.
        if (player.equals(table[position]) && ignore) {
            return;
        }

        //ставим фишку
        table[position] = player;

        //двигаемся в каждом возможном направлении
        for (int direction : new int[]{-9, -8, -7, -1, 1, 7, 8, 9}) {
            int nextPosition = position + direction;
            //если ход в пределах таблицы и мы двигаемся в направлении врагов
            if (canMove(direction, nextPosition) && Boolean.valueOf(!player).equals(table[nextPosition])) {
                //двигаемся пока можем
                for (int i = nextPosition + direction; canMove(direction, i); i += direction) {
                    //упс, пустая ячейка. не повезло, валим отсюда
                    if (table[i] == null) {
                        break;
                    }
                    //наша фишечка стоит в конце, значит, можем двигаться в этом направлении,
                    //попутно превращая врагов в своих
                    if (player.equals(table[i])) {
                        apply(table, nextPosition, player, ignore);
                        break;
                    }
                }
            }
        }
    }

    public static boolean canMove(int direction, int to) {
        // мы не должны выйти за пределы массива от 0 до 64,
        return to >= 0 && to < 64 &&
                //не можем двигаться вправо, если находимся на правом краю доски
                ((direction != 9 && direction != -7 && direction != 1) || to % 8 != 0) &&
                //не можем двигаться влево, если находимся на левом краю доски
                ((direction != -9 && direction != 7 && direction != -1) || to % 8 != 7);
    }

    public static int score(Boolean[] table, Boolean player) {
        int s = 0;

        //просто посчитаем, сколько наших фишек
        for (Boolean pos : table) {
            s += player.equals(pos) ? 1 : 0;
        }

        return s;
    }
}
