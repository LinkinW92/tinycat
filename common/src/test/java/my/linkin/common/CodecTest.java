package my.linkin.common;

import com.alibaba.fastjson.JSON;
import my.linkin.entity.Entity;
import my.linkin.entity.Heartbeat;
import my.linkin.entity.TiCommand;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author chunhui.wu
 */
public class CodecTest {
    @Test
    public void testCodec() {
        TiCommand cmd = TiCommand.heartbeat();
        Heartbeat beat = new Heartbeat("ping");
        beat.extent("h111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111ello", "wor111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111ld");
        beat.extent("libai","第一种说法：那是李白七岁时，父亲要给儿子起个正式的名字。李白的父母亲酷爱读书，他们要培养儿子做个高雅脱俗的人。父亲平时喜欢教孩子看书作诗，在酝酿起名之时，同母亲商量好了，就在庭院散步时考考儿子作诗的能力。\n" +
                "父亲看着春日院落中葱翠树木，似锦繁花，开口吟诗道：“春国送暖百花开，迎春绽金它先来。”母亲接着道：“火烧叶林红霞落”。李白知道父母吟了诗句的前三句，故意留下最后一句，希望自己接续下去。他走到正在盛开的李树花前，稍稍想了一下说：“李花怒放一树白”。\n" +
                "“白”——不正说出了李花的圣洁高雅吗？父亲灵机一动，决定把妙句的头尾“李”“白”二字选作孩子的名字，便为七岁的儿子取名为“李白”。\n" +
                "第二种说法：李阳冰的《草堂集》序中说：“逃归于蜀，复指李树而生伯阳。惊姜之夕，长庚入梦。故生而名白，以太白字之”。范传正的《唐左拾遗翰林学士李公新墓碑》中写道：“公之生也，先府君指天(李)枝以复姓，先夫人梦长庚而告祥，名之与字，咸取所象”。\n" +
                "从以上文献中我们不难看出，李白的姓名是其父回到蜀中给自己恢复了李姓后，为后来出世的李白取的名字，李母梦到太白金星后于是有了身孕，在阵痛难忍中生出李白，李白的名字就是根据这一梦境得来的。\n" +
                "唐朝长安元年，李白出生于武则天执政后期的公元701年。李白少年时代的学习内容很广泛，除儒家经典、古代文史名著外，还浏览诸子百家之书。他很早就相信喜欢道教，喜欢隐居山林，求仙学道；同时又有建功立业的政治抱负。一方面要做超脱尘俗的隐士神仙，一方面要做君主的辅弼大臣，这就形成了出世与入世的矛盾。但积极入世、关心国家，是其一生思想的主流，也是构成他作品进步内容的思想基础。李白青少年时期在蜀地所写诗歌，留存很少，但像《访戴天山道士不遇》《峨眉山月歌》等篇，已显示出突出的才华。");
        cmd.setBody(beat.encode());
        ByteBuffer buffer = cmd.encode();
        TiCommand cmd2 = TiCommand.decode(buffer);
        System.out.println(JSON.toJSONString(Entity.decode(Heartbeat.class, cmd2.getBody())));
        System.out.println(JSON.toJSONString(cmd2.getHeader()));
        // test for requestId
        TiCommand cmd3 = TiCommand.request();
        System.out.println(JSON.toJSONString(cmd3.getHeader()));
        TiCommand cmd4 = TiCommand.request();
        System.out.println(JSON.toJSONString(cmd4.getHeader()));
    }
}
