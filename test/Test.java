import java.math.BigInteger;

public class Test
{
    BigInteger num = new BigInteger("1111111");
    BigInteger num2 = BigInteger.valueOf(22222L);

    String test = "test string";
    String test2 = "test2 " + test + " - string, " + num + " - num, " + num2 + " - num2";
    String test3 = num2 + " - num2";

    static class Item
    {
        public String name()
        {
            return "test name";
        }
    }

    static class OptLang
    {
        static Item EN = new Item();
    }

    static class CardTovLocalization
    {
        static Item NAME = new Item();
    }

    static class SqlTools
    {
        public static String forSQL(String name)
        {
            return name + " (forSQL)";
        }
    }

    String sql = "SELECT\n" +
        "     f.f_cod,\n" +
        "     r.f_name,\n" +
        "     (\n" +
        "         SELECT\n" +
        "             rl.f_text\n" +
        "         FROM\n" +
        "             rest_localization rl\n" +
        "         WHERE\n" +
        "             rl.f_language = " + SqlTools.forSQL(OptLang.EN.name()) + "\n" +
        "             AND rl.f_attribute = " + SqlTools.forSQL(CardTovLocalization.NAME.name()) + "\n" +
        "             AND rl.f_tov_code = r.f_cod\n" +
        "     ) AS name_en,\n" +
        " FROM\n";
}
