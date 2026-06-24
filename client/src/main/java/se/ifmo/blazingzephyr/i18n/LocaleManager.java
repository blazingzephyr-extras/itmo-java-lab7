package se.ifmo.blazingzephyr.i18n;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Синглтон для управления локалью приложения.
 *
 * Хранит текущий Locale и ResourceBundle в Observable-свойствах,
 * чтобы UI мог реагировать на изменения без перезапуска.
 *
 * Использование:
 *   LocaleManager.getInstance().setLocale(Locale.of("it"));
 *   String s = LocaleManager.getInstance().get("auth.login");
 */
public class LocaleManager {

    public static final Locale RUSSIAN    = Locale.of("ru");
    public static final Locale ESTONIAN   = Locale.of("et");
    public static final Locale ITALIAN    = Locale.of("it");
    public static final Locale SALVADORAN = Locale.of("es", "SV");

    public static final List<Locale> SUPPORTED = List.of(
        RUSSIAN, ESTONIAN, ITALIAN, SALVADORAN
    );

    private static final String BUNDLE_BASE = "se.ifmo.blazingzephyr.i18n.Messages";
    private static final LocaleManager INSTANCE = new LocaleManager();

    public static LocaleManager getInstance() {
        return INSTANCE;
    }

    private final ObjectProperty<Locale> localeProperty =
        new SimpleObjectProperty<>(RUSSIAN);

    private final ObjectProperty<ResourceBundle> bundleProperty =
        new SimpleObjectProperty<>();

    private LocaleManager() {
        // Загружаем бандл при инициализации
        localeProperty.addListener((obs, oldL, newL) -> reloadBundle(newL));
        reloadBundle(RUSSIAN);
    }

    private void reloadBundle(Locale locale) {
        Locale.setDefault(locale);
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);
        bundleProperty.set(bundle);
    }

    public String get(String key) {
        return bundleProperty.get().getString(key);
    }

    public void setLocale(Locale locale) {
        localeProperty.set(locale);
    }

    public Locale getLocale() {
        return localeProperty.get();
    }

    public ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    public ObjectProperty<ResourceBundle> bundleProperty() {
        return bundleProperty;
    }

    // ---------- форматирование чисел и дат ----------
    /**
     * Форматирует число в соответствии с текущей локалью.
     * Например: 1 234 567,89 (ru) / 1.234.567,89 (it) / 1,234,567.89 (es_SV)
     */
    public String formatNumber(Number number) {
        return NumberFormat.getNumberInstance(getLocale()).format(number);
    }

    /**
     * Форматирует денежное значение в соответствии с текущей локалью.
     */
    public String formatCurrency(Number amount) {
        return NumberFormat.getCurrencyInstance(getLocale()).format(amount);
    }

    /**
     * Возвращает DateTimeFormatter для отображения дат в текущей локали.
     * Пример: 04.06.2026 (ru/et) / 4 giugno 2026 (it) / 4 de junio de 2026 (es_SV)
     */
    public DateTimeFormatter dateFormatter() {
        return DateTimeFormatter
            .ofLocalizedDate(FormatStyle.LONG)
            .withLocale(getLocale());
    }

    /**
     * Возвращает DateTimeFormatter для отображения даты и времени.
     */
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(getLocale());
    }

    /**
     * Человекочитаемое название локали (из бандла), например "Italiano".
     */
    public String getDisplayName(Locale locale) {
        try {
            String key = "lang." + locale.getLanguage()
                + (locale.getCountry().isEmpty() ? "" : "_" + locale.getCountry());
            return bundleProperty.get().getString(key);
        } catch (Exception e) {
            return locale.getDisplayName(getLocale());
        }
    }
}