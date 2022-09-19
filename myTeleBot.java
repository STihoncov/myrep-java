import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

public class myTeleBot {
    public static void main(String[] args) throws IOException {
        //   5449772306:AAHK7cIrgqM32ufx1KBKHPDMjJQHrVvulnI     ТОКЕН БОТА
        TelegramBot bot = new TelegramBot("5449772306:AAHK7cIrgqM32ufx1KBKHPDMjJQHrVvulnI");
//  входящие сообщения и ответ
        inputNumberInfo(bot);

    }

    private static void inputNumberInfo(TelegramBot bot) {
        bot.setUpdatesListener(updates -> {
            updates.forEach(upd -> {
                try {
                    System.out.println(upd);
                    long chatId = upd.message().chat().id();
                    String senderName = upd.message().from().firstName();
                    String incomeMessage = upd.message().text();
                    String message = "Здравствуйте!" + senderName + "!\nЧто хотите посмотреть: \n1.Курс валют \n2.Снимки NASA \n3.Анекдот \nВведите цифру 1, 2, 3";
                    SendMessage request = new SendMessage(chatId, message);
                    bot.execute(request);
                    if (incomeMessage.equals("1")) {
                        messageValute(bot, chatId);
                    } else if (incomeMessage.equals("2")) {
                        messageNASAfoto(bot, chatId);
                    } else if (incomeMessage.equals("3")) {
                        messageFunStory(bot, chatId);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void messageFunStory(TelegramBot bot, long chatId) throws IOException {
        Document doc = Jsoup.connect("http://rzhunemogu.ru/Rand.aspx?CType=1").get();
        System.out.println(doc.title());
        Elements story = doc.select("content");
        String messageStory = story.text();
        SendMessage requestFhoto = new SendMessage(chatId, messageStory);
        bot.execute(requestFhoto);
    }

    private static void messageNASAfoto(TelegramBot bot, long chatId) {
        String date = getRandomDateStr();
        String url = "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY&date=" + date;
        String page = downloadWebpage(url);

        String hdUrl = getTagValue(page, "hdurl");
        String title = getTagValue(page, "title");

        String messageFhoto = title + "\n" + hdUrl;
        SendMessage requestFhoto = new SendMessage(chatId, messageFhoto);
        bot.execute(requestFhoto);
    }

    private static void messageValute(TelegramBot bot, long chatId) {
        String date = getTodayDate();
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + date).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(doc.title());
        Elements valutes = doc.select("Valute");
        String resultUSD = "";
        String resultEUR = "";
        for (Element valute : valutes) {
            if (valute.attr("ID").equals("R01235")) {
                resultUSD = "USD : " + valute.select("Value").text();
                // System.out.println(result);
            } else if (valute.attr("ID").equals("R01239")) {
                resultEUR = "EUR : " + valute.select("Value").text();
                // System.out.println(result);
            }
        }
        String massageValute = "Курс валют на сегодня\n" + date + "\n" + resultUSD + "\n" + resultEUR;
        SendMessage requestValute = new SendMessage(chatId, massageValute);
        bot.execute(requestValute);
    }

    @NotNull
    private static String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date analogDate = new Date();
        String parseDate = dateFormat.format(analogDate);
        String date = parseDate.replaceAll("-", "/");
        return date;
    }

    private static String getRandomDateStr() {
        RandomDateGenerator randomDateGenerator = new RandomDateGenerator();
        LocalDate randomDate = randomDateGenerator.getRandomDate(1996, 2021);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateStr = randomDate.format(formatter);
        return dateStr;
    }
    private static String getTagValue(String page, String tagName) {
        int begin = page.indexOf('"' + tagName + '"') + 4 + tagName.length();
        int end = page.indexOf('"',begin);
        String sub = page.substring(begin,end);
        return sub;
    }
    public static String downloadWebpage(String url) {
        StringBuilder result = new StringBuilder();
        String line;
        URLConnection urlConnection = null;
        try {
            urlConnection = new URL(url).openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (InputStream is = urlConnection.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  result.toString();
    }
    public static class RandomDateGenerator {
        public LocalDate getRandomDate (int startYer, int endYer) {
            int difference = endYer - startYer;
            int randomYerDelta = new Random().nextInt(difference + 1);
            int randomYer = 2000 + randomYerDelta;
            int randomMonth = new Random().nextInt(12) + 1;
            int randomDay = new Random().nextInt(28) + 1;
            return LocalDate.of(randomYer, randomMonth, randomDay);
        }
    }

}
