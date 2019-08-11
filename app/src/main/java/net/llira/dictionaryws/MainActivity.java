package net.llira.dictionaryws;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
  private EditText txtWord;
  private TextView txtText;
  private Button btnSearch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //Inflate
    txtWord = findViewById(R.id.txt_word);
    txtText = findViewById(R.id.txt_text);
    btnSearch = findViewById(R.id.btn_search);
    btnSearch.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String word = "";
        word = txtWord.getText().toString();
        new AccessWebServiceTask().execute(word);
      }
    });
  }

  private String wordDefinition(String word) {
    InputStream in = null;
    String strDefinition = "";
    try {
      in = openHttpConnection("http://services.aonaware.com/" +
              "DictService/DictService.asmx/Define?word=" + word);
      Document doc = null;
      DocumentBuilderFactory dbf;
      dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db;
      try {
        db = dbf.newDocumentBuilder();
        doc = db.parse(in);
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
      doc.getDocumentElement().normalize();
      NodeList definitionElements =
              doc.getElementsByTagName("Definition");
      for (int i = 0; i < definitionElements.getLength(); i++) {
        Node itemNode = definitionElements.item(i);
        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
          Element definitionElement = (Element) itemNode;
          NodeList wordDefinitionElements =
                  (definitionElement).getElementsByTagName("WordDefinition");
          strDefinition = "";
          for (int j = 0; j < wordDefinitionElements.getLength(); j++) {
            Element wordDefinitionElement = (Element) wordDefinitionElements.item(j);
            NodeList textNodes;
            textNodes = ((Node) wordDefinitionElement).getChildNodes();
            strDefinition += ((Node) textNodes.item(0)).getNodeValue() + "\n";
          }
        }
      }
    } catch (IOException e) {
      Log.d("MainActivity", e.getLocalizedMessage());
    }
    return strDefinition;
  }

  private InputStream openHttpConnection(String urlString) throws IOException {
    InputStream in = null;
    int res = -1; // response
    URL url = new URL(urlString);
    URLConnection conn = url.openConnection();
    if (!(conn instanceof HttpURLConnection)) {
      throw new IOException("Not is HTTP connection.");
    }
    try {
      HttpURLConnection httpURLConn = (HttpURLConnection) conn;
      httpURLConn.setAllowUserInteraction(false);
      httpURLConn.setInstanceFollowRedirects(true);
      httpURLConn.setRequestMethod("GET");
      httpURLConn.connect();
      res = httpURLConn.getResponseCode();
      if (res == HttpURLConnection.HTTP_OK) {
        in = httpURLConn.getInputStream();
      }
    } catch (Exception e) {
      Log.d("Networking", e.getLocalizedMessage());
      throw new IOException("Error in connection");
    }
    return in;
  }

  private class AccessWebServiceTask extends
          AsyncTask<String, Void, String> {
    protected String doInBackground(String... urls) {
      return wordDefinition(urls[0]);
    }

    protected void onPostExecute(String result) {
      txtText.setText(result);
    }
  }

}//End class
