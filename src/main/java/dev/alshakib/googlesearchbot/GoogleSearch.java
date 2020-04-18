package dev.alshakib.googlesearchbot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GoogleSearch {

    private InlineQuery inlineQuery;

    public GoogleSearch() {
        this.inlineQuery = null;
    }

    public GoogleSearch(InlineQuery inlineQuery) {
        this.inlineQuery = inlineQuery;
    }

    public void setInlineQuery(InlineQuery inlineQuery) {
        this.inlineQuery = inlineQuery;
    }

    public InlineQuery getInlineQuery() {
        return this.inlineQuery;
    }

    private Elements fetchWebResultElements(String query) throws Exception {
        if (!query.isEmpty()) {
            Document document = Jsoup.connect("https://www.google.com/search?num=50&hl=EN&q=" + URLEncoder.encode(query, String.valueOf(StandardCharsets.UTF_8)))
                    .userAgent("Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:75.0) Gecko/20100101 Firefox/75.0")
                    .referrer("https://www.google.com")
                    .followRedirects(true)
                    .get();
            return document.getElementsByClass("rc");
        } else {
            throw new NullPointerException();
        }
    }

    public ArrayList<InlineQueryResult> getSearchResults() {
        ArrayList<InlineQueryResult> searchResults = new ArrayList<>();
        try {
            Elements searchResultElements = fetchWebResultElements(inlineQuery.getQuery());
            if (searchResultElements.isEmpty()) {
                InputTextMessageContent messageContent = new InputTextMessageContent();
                messageContent.setDisableWebPagePreview(true);
                messageContent.enableHtml(true);
                messageContent.setMessageText("No results found for <b>" + inlineQuery.getQuery() + "</b>");
                InlineQueryResultArticle article = new InlineQueryResultArticle();
                article.setInputMessageContent(messageContent);
                article.setId("NoResultFound");
                article.setTitle("No results found for " + inlineQuery.getQuery());
                searchResults.add(article);
                return searchResults;
            }
            int articleCount = 0;
            for (Element element : searchResultElements) {
                String resultTitle = element.getElementsByClass("r").get(0).getElementsByTag("h3").text().trim();
                String resultDescription = element.getElementsByClass("st").text().trim();
                String resultURL = element.getElementsByClass("r").get(0).getElementsByTag("a").get(0).attr("abs:href").trim();
                if (!resultTitle.equals("")) {
                    InputTextMessageContent messageContent = new InputTextMessageContent();
                    messageContent.enableHtml(true);
                    messageContent.setMessageText("<a href='" + resultURL + "'>" + resultTitle + "</a>");
                    InlineQueryResultArticle article = new InlineQueryResultArticle();
                    article.setInputMessageContent(messageContent);
                    article.setId(inlineQuery.getId() + articleCount);
                    article.setTitle(resultTitle);
                    article.setDescription(resultDescription);
                    article.setUrl(resultURL);
                    searchResults.add(article);
                    ++articleCount;
                }
                if (articleCount >= 50) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            InputTextMessageContent errorMessage = new InputTextMessageContent();
            errorMessage.setMessageText("Could not fetch search result. Try again.");
            InlineQueryResultArticle errorArticle = new InlineQueryResultArticle();
            errorArticle.setInputMessageContent(errorMessage);
            errorArticle.setId("SomethingWentWrong");
            errorArticle.setTitle("Could not fetch search result. Try again.");
            searchResults.add(errorArticle);
            return searchResults;
        }
        return searchResults;
    }
}
