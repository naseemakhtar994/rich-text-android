

package io.square1.richtextlib.v2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.text.TextUtils;
import android.util.Patterns;


import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;


import io.square1.richtextlib.EmbedUtils;
import io.square1.richtextlib.v2.content.RichTextDocumentElement;
import io.square1.richtextlib.R;
import io.square1.richtextlib.v2.content.DocumentElement;
import io.square1.richtextlib.v2.content.OembedElement;
import io.square1.richtextlib.v2.content.RichDocument;
import io.square1.richtextlib.v2.parser.InternalContentHandler;
import io.square1.richtextlib.v2.parser.MarkupContext;
import io.square1.richtextlib.v2.parser.MarkupTag;
import io.square1.richtextlib.style.*;
import io.square1.richtextlib.v2.parser.TagHandler;
import io.square1.richtextlib.v2.utils.SpannedBuilderUtils;

/**
 * This class processes HTML strings into displayable styled text.
 * Not all HTML tags are supported.
 */
public class RichTextV2 {

    public static final String TAG = "RICHTXT";

    public static class V2DefaultStyle implements  Style {

        private static final float[] HEADER_SIZES = {
                1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
        };

        private Context mApplicationContext;
        private Bitmap mQuoteBitmap;
        private int mMaxImageWidth;

        public V2DefaultStyle(Context context){
            mApplicationContext = context.getApplicationContext();
            Resources resources = context.getResources();
            mQuoteBitmap = BitmapFactory.decodeResource(resources, R.drawable.quote);
            mMaxImageWidth = resources.getDisplayMetrics().widthPixels;
        }

        @Override
        public Context getApplicationContext(){
            return mApplicationContext;
        }
        @Override
        public Bitmap quoteBitmap() {
            return mQuoteBitmap;
        }

        @Override
        public int getQuoteBackgroundColor() {
            return Color.parseColor("#d3d3d3");
        }

        @Override
        public int headerColor() {
            return Color.BLACK;
        }

        @Override
        public int backgroundColor() {
            return Color.WHITE;
        }

        @Override
        public int maxImageWidth() {
            return mMaxImageWidth;
        }

        @Override
        public int maxImageHeight() {
            return Style.USE_DEFAULT;
        }

        @Override
        public float headerIncrease(int headerLevel) {
            return HEADER_SIZES[headerLevel];
        }

        @Override
        public float smallTextReduce() {
            return 0.8f;
        }

        @Override
        public boolean parseWordPressTags(){
            return true;
        }

        @Override
        public boolean treatAsHtml(){
            return true;
        }

    }


    private static class HtmlParser {
        private static final HTMLSchema schema = new HTMLSchema();
    }



    private ArrayList<DocumentElement> mResult = new ArrayList<>();
    private Stack<MarkupTag> mStack = new Stack<>();
    private Stack<MarkupContext> mMarkupContextStack = new Stack<>();
    private RichTextDocumentElement mOutput;
    private MarkupContext mCurrentContext;

    private RichTextV2(Context context) {
        mCurrentContext = new MarkupContext(this, new V2DefaultStyle(context));
        mMarkupContextStack.push(mCurrentContext);

        mOutput = new RichTextDocumentElement();
        mResult = new ArrayList<>();
    }


    public Style getCurrentStyle(){
        return mCurrentContext.getStyle();
    }

    public RichTextDocumentElement getCurrentOutput(){
        return mOutput;
    }


    /**
     * Returns displayable styled text from the provided HTML string.
     * Any &lt;img&gt; tags in the HTML will use the specified ImageGetter
     * to request a representation of the image (use null if you don't
     * want this) and the specified TagHandler to handle unknown tags
     * (specify null if you don't want this).
     *
     * <p>This uses TagSoup to handle real HTML, including all of the brokenness found in the wild.
     */


    public static RichDocument fromHtml(Context context, String source) {

        final Style defaultStyle = new V2DefaultStyle(context);
       return fromHtmlImpl(context, source, defaultStyle);

    }

    public static  RichDocument  fromHtml(Context context, String source, Style style) {
       return fromHtmlImpl(context, source, style);

    }

    final static String SOUND_CLOUD = "\\[soundcloud (.*?)/?\\]";
    final static String SOUND_CLOUD_REPLACEMENT = "<soundcloud $1 />";

    final static String INTERACTION = "\\[interaction (.*?)/?\\]";
    final static String INTERACTION_REPLACEMENT = "";


    private static RichDocument fromHtmlImpl(Context context,
                                                         String source,
                                                         Style style)  {




        try {

            XMLReader reader = new Parser();
            reader.setProperty(Parser.schemaProperty, HtmlParser.schema);

            if(style.parseWordPressTags() == true) {

                //soundcloud
                //source = source.replaceAll("\\[/soundcloud\\]","");

                source = source.replaceAll(SOUND_CLOUD,
                        SOUND_CLOUD_REPLACEMENT);

                source = source.replaceAll(INTERACTION,
                        INTERACTION_REPLACEMENT);

               // Matcher m = pattern.matcher(source);
              //  while (m.find() == true) {
              //      m.groupCount();
              //  }
            }



            RichTextV2 richText = new RichTextV2(context);
            reader.setContentHandler(new InternalContentHandler(richText));
            reader.parse(new InputSource(new StringReader(source)));
            richText.appendRemainder();
            RichDocument out = new RichDocument("",richText.mResult);
            return out;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return RichDocument.EMPTY;
    }



    public void startElement(String uri, String localName, Attributes atts, String textContent) {

        processAccumulatedTextContent(textContent);
        MarkupTag tag = new MarkupTag(localName,atts);
        mStack.push(tag);
        mCurrentContext = mCurrentContext.onTagOpen(tag, mOutput, false);
        mMarkupContextStack.push( mCurrentContext );
    }

    public void endElement(String uri, String localName, String textContent) {

        MarkupTag tag = mStack.pop();

        if(tag.getTagHandler().processContent() == true) {
            processAccumulatedTextContent(textContent);
        }



        if(tag.tag.equalsIgnoreCase(localName) == true) {
            mCurrentContext.onTagClose(tag, mOutput, false);
        }
        mCurrentContext = mMarkupContextStack.pop();
    }


    private RichTextDocumentElement processAccumulatedTextContent(String accumulatedText)  {

        if(TextUtils.isEmpty(accumulatedText)){
            return null;
        }

        Matcher m = Patterns.WEB_URL.matcher(accumulatedText);

        while (m.find()) {
            //link found
            int matchStart = m.start();
            int matchEnd = m.end();

            //any text in between ?
            StringBuffer buffer = new StringBuffer();
            m.appendReplacement(buffer, "");
            mOutput.append(buffer);

            CharSequence link = accumulatedText.subSequence( matchStart, matchEnd);
            // if( EmbedUtils.parseLink(mAccumulatedText, String.valueOf(link), this) == false){
            if( EmbedUtils.parseLink(accumulatedText, String.valueOf(link), new EmbedUtils.ParseLinkCallback() {
                @Override
                public void onLinkParsed(Object callingObject, String result, EmbedUtils.TEmbedType type) {
                    if(type == EmbedUtils.TEmbedType.EYoutube){
                        SpannedBuilderUtils.makeYoutube(result, getCurrentStyle().maxImageWidth(), mOutput);
                    }else{
                        onEmbedFound(type, result);
                    }
                }
            }) == false){

                if(TextUtils.isEmpty(link) == false) {
                    SpannedBuilderUtils.makeLink(link.toString(), null, mOutput);
                    //int where = mSpannableStringBuilder.length();
                    // mSpannableStringBuilder.append(link);
                    // mSpannableStringBuilder.setSpan(new URLSpan(link.toString()), where, where + link.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                // }

            }


        }
        StringBuffer buffer = new StringBuffer();
        m.appendTail(buffer);
        mOutput.append(buffer);

        return mOutput;
    }

    public void onEmbedFound(EmbedUtils.TEmbedType type, String content){

        RichTextDocumentElement newOut = new RichTextDocumentElement();
        /// close output
        if(mOutput != null &&
                mOutput.length() > 0){

            for(int index = (mStack.size() - 1); index >= 0 ; index --){
               mCurrentContext.onTagClose(mStack.get(index), mOutput, true);
            }

            SpannedBuilderUtils.fixFlags(mOutput);
            mResult.add(mOutput);

            //create new Output
        for(int index = 0; index < mStack.size(); index ++){
                mCurrentContext.onTagOpen(mStack.get(index), newOut, true );
            }

            mOutput = newOut;
        }

        mResult.add(OembedElement.newInstance(type, content));

    }

    public MarkupTag getParent(MarkupTag child, Class<? extends TagHandler> parentClass) {

        boolean childFound = false;

        for(int index = (mStack.size() - 1); index >= 0 ; index --){

            MarkupTag current = mStack.get(index);

            if(childFound == false){
                childFound = (current == child);
            }else {
                TagHandler handler = current.getTagHandler();
                if(parentClass.isAssignableFrom(handler.getClass())){
                    return current;
                }
            }



//            //while walking back on the stack first we find the child
//            // then look at the next element in the stack which will be the parent
//
//            if(childFound == true) {
//                if (parent != child) return parent;
//            }else {
//                childFound = (parent != child);
//            }
        }

        return null;
    }



    private void appendRemainder(){
        if(mOutput != null &&
                mOutput.length() > 0){
            SpannedBuilderUtils.fixFlags(mOutput);
            mResult.add(mOutput);
        }
    }


}
