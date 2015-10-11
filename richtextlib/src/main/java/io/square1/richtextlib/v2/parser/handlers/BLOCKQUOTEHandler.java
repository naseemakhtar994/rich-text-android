package io.square1.richtextlib.v2.parser.handlers;

import android.graphics.Typeface;
import android.text.Spannable;

import io.square1.richtextlib.v2.content.RichTextDocumentElement;
import io.square1.richtextlib.style.QuoteSpan;
import io.square1.richtextlib.style.Style;
import io.square1.richtextlib.style.StyleSpan;
import io.square1.richtextlib.v2.parser.MarkupContext;
import io.square1.richtextlib.v2.parser.MarkupTag;
import io.square1.richtextlib.v2.parser.TagHandler;
import io.square1.richtextlib.v2.utils.SpannedBuilderUtils;

/**
 * Created by roberto on 04/09/15.
 */
public class BLOCKQUOTEHandler extends TagHandler {

    @Override
    public void onTagOpen(MarkupContext context, MarkupTag tag, RichTextDocumentElement out) {

        SpannedBuilderUtils.ensureAtLeastThoseNewLines(out, 2);
        SpannedBuilderUtils.startSpan(out, new Markers.Blockquote(tag.elementClasses));
       // handleP(spannable);
    }

    @Override
    public void onTagClose(MarkupContext context, MarkupTag tag, RichTextDocumentElement out) {

        //TODO handle Tweets
        Markers.Blockquote obj = out.getLastSpan(Markers.Blockquote.class);
        if(obj == null) return;

        SpannedBuilderUtils.ensureAtLeastThoseNewLines(out, 2);

        int len = out.length();
        int where = out.getSpanStart(obj);
        out.removeSpan(obj);

            if (where != len) {

                Style style = context.getStyle();

                StyleSpan styleSpan = new StyleSpan(Typeface.ITALIC);
                out.setSpan(styleSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                QuoteSpan quoteSpan = new QuoteSpan(style.getQuoteBackgroundColor(), style.quoteBitmap());
                out.setSpan(quoteSpan, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
    }
}