package com.freeroom.web;

import com.freeroom.di.BeanContext;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import static com.freeroom.util.RequestBuilder.one;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class AresTest
{
    private BeanContext beanContext;

    @Before
    public void setUp()
    {
        beanContext = BeanContext.load("com.freeroom.web.beans");
    }

    @Test
    public void should_return_plain_html()
    {
        final Hephaestus hephaestus = new Hephaestus(beanContext, one().uri("/").build());
        final Ares ares = new Ares(hephaestus.getHandler().fst, hephaestus.getHandler().snd);
        assertThat(readText(ares.getContent()), containsString("Hello World!"));
    }

    private String readText(final FileChannel content)
    {
        StringBuilder sb = new StringBuilder();
        try {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            Charset charset = Charset.forName("UTF-8");
            while(content.read(byteBuffer) != -1) {
                byteBuffer.flip();
                sb.append(charset.decode(byteBuffer));
                byteBuffer.clear();
            }
            content.close();
        } catch (Exception ignored) {}
        return sb.toString();
    }
}
