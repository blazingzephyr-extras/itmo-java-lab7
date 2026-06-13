package se.ifmo.blazingzephyr.utility;

import javafx.scene.input.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import se.ifmo.blazingzephyr.model.OrganizationWithId;

public class CanvasUtility {
    
    // Обрабатывает нажатие на точку на канве.
    public static void onMouseClicked(
        MouseEvent event,
        Canvas canvas,
        TableView<OrganizationWithId> view,
        Consumer<OrganizationWithId> click,
        Consumer<OrganizationWithId> doubleClick
    ) {
        // Координата точки
        double  mx = event.getX(),
                my = event.getY();

        for (OrganizationWithId org : view.getItems()) {
                double x = toCanvasCoord(org.getData().getCoordinates().getX(), canvas.getWidth());
                double y = toCanvasCoord(org.getData().getCoordinates().getY(), canvas.getHeight());
                double size = 20;

                // Проверяем попадание в круг
                if (mx >= x && mx <= x + size && my >= y && my <= y + size) {

                    // Двойное нажатие.
                    if (event.getClickCount() == 2) {
                        doubleClick.accept(org);
                    }

                    // Одиночное нажатие.
                    else {
                        click.accept(org);
                    }

                    break;
                }
            }
    }
    
    // Цвета для разных пользователей
    private static final Map<String, Color> userColors = new HashMap<>();
    private static final List<Color> palette = List.of(
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.PINK
    );

    // Назначает пользователю доступный цвет.
    private static Color getColorForUser(String owner) {
        return userColors.computeIfAbsent(owner, k -> palette.get(userColors.size() % palette.size()));
    }

    /**
     * Переводит координату модели в координату канваса.
     *
     * Проблема: координаты объектов могут быть отрицательными или очень большими.
     * Java-остаток (%) от отрицательного числа отрицателен — точка уходит
     * за пределы канваса и рисуется поверх других элементов сцены.
     *
     * Решение: берём Math.abs() и масштабируем через % — точка всегда внутри.
     * Добавляем padding, чтобы круг не прилипал к краю.
     */
    private static double toCanvasCoord(double modelValue, double canvasSize) {
        double padding = 20;
        double range = canvasSize - padding * 2;
        return (Math.abs(modelValue) % range) + padding;
    }

    // Перерисовывает канвас.
    public static void redrawCanvas(
        Canvas canvas,
        List<OrganizationWithId> orgs
    ) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (OrganizationWithId org : orgs) {
            double x = toCanvasCoord(org.getData().getCoordinates().getX(), canvas.getWidth());
            double y = toCanvasCoord(org.getData().getCoordinates().getY(), canvas.getHeight());
            double size = org.getData().getAnnualTurnover() != null
                ? Math.min(org.getData().getAnnualTurnover() / 1000, 50) + 10
                : 20;

            Color color = getColorForUser(org.getOwner());
            gc.setFill(color);

            // Рисуем круг
            gc.fillOval(x, y, size, size);

            // Подпись
            gc.setFill(Color.BLACK);
            gc.fillText(org.getData().getName(), x, y - 5);
        }
    }

    // Анимирует объект при его появлении.
    public static void animateOrg(Canvas canvas, OrganizationWithId org) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double x = toCanvasCoord(org.getData().getCoordinates().getX(), canvas.getWidth());
        double y = toCanvasCoord(org.getData().getCoordinates().getY(), canvas.getHeight());
        Color color = getColorForUser(org.getOwner());

        // Анимация - круг увеличивается от 0 до нужного размера.
        double targetSize = 20;
        AnimationTimer timer = new AnimationTimer() {
            double size = 0;

            @Override
            public void handle(long now) {
                size += 0.05;
                gc.setFill(color);
                gc.fillOval(x, y, size, size);
                
                if (size >= targetSize) {
                    stop();
                }
            }
        };
        timer.start();
    }
}
