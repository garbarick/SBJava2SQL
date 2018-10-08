public class Test
{
    String test = "test string";
    String test2 = "test2 " + test + " string2";

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
