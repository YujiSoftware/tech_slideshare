package tech.slideshare.crawler;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpeakerDeckCrawlerTest {
    @Test
    void crawl() throws IOException {
        SpeakerDeckCrawler crawler = new SpeakerDeckCrawler();
        List<String> actual = crawler.crawl("https://speakerdeck.com/smiyawaki0820/2023-dot-08-dot-07-geography-and-language-mian-qiang-hui-number-4");

        assertEquals(80, actual.size());
        assertEquals("""
                LLM によるプログラムベース推論
                Shumpei Miyawaki
                keywalker,inc. / Tohoku Univ.
                @catshun_
                2023.08.07 Geography&Language 勉強会 #4
                https://sites.google.com/view/geography-and-language/studygroup
                • LLM 活用事例の一つとして プログラムベース推論の事例 を紹介
                • 勉強会を通してプログラムベース推論の長所・短所を議論し、
                LLM 活用の選択肢を増やす ことに議論を終着させる""", actual.get(0));
        assertEquals("""
                P.80
                • Ribeiro+’23 - Testing Language Models (and Prompts) Like We Test Software / TowardsDataScience Blog
                https://towardsdatascience.com/testing-large-language-models-like-we-test-software-92745d28a359
                • James Murdza+’23 - Evaluating code generation agents — LangChain and CodeChain
                https://medium.com/@jamesmurdza/evaluating-llms-on-code-generation-langchain-and-codechain-5a804cb1e31c
                LLM × テスト""", actual.get(79));
    }

    @Test
    void mojibake() throws IOException {
        SpeakerDeckCrawler crawler = new SpeakerDeckCrawler();
        List<String> actual = crawler.crawl("https://speakerdeck.com/01rabbit/mr-dot-rabbit-wen-itashi-haarukedo-shi-ji-nihajian-tashi-ganaihatukingugazietuto");

        assertEquals(31, actual.size());
        assertEquals("""
                聞いた事はあるけど、実際には見た事がない
                ハッキングガジェット
                2017/12/2
                元祖 濱せっく #1
                @01ra66it""", actual.get(0));
        assertEquals("""
                厨二病の妄想と思った方は笑ってやってくださいw
                もし、私と同じ様に脅威となりうると感じた方がいらっ
                しゃいましたら一緒に対応策を考えてみませんか？
                おわり""", actual.get(30));
    }
}