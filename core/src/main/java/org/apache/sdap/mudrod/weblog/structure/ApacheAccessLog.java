/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sdap.mudrod.weblog.structure;

import com.google.gson.Gson;

import org.apache.sdap.mudrod.main.MudrodConstants;
import org.apache.sdap.mudrod.weblog.pre.CrawlerDetection;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an Apache access log line. See
 * http://httpd.apache.org/docs/2.2/logs.html for more details.
 */
public class ApacheAccessLog extends WebLog implements Serializable {
  String Response;
  String Referer;
  String Browser;

  @Override
  public double getBytes() {
    return this.Bytes;
  }

  public String getBrowser() {
    return this.Browser;
  }

  public String getResponse() {
    return this.Response;
  }

  public String getReferer() {
    return this.Referer;
  }

  public ApacheAccessLog() {
	  super();
  }

  public static String parseFromLogLine(String log, Properties props) throws IOException, ParseException {

    String logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+|-) \"((?:[^\"]|\")+)\" \"([^\"]+)\"";
    final int numFields = 9;
    Pattern p = Pattern.compile(logEntryPattern);
    Matcher matcher;

    String lineJson = "{}";
    matcher = p.matcher(log);
    if (!matcher.matches() || numFields != matcher.groupCount()) {
      return lineJson;
    }

    String time = matcher.group(4);
    time = SwithtoNum(time);
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
    Date date = formatter.parse(time);

    String bytes = matcher.group(7);

    if ("-".equals(bytes)) {
      bytes = "0";
    }

    String request = matcher.group(5).toLowerCase();
    String agent = matcher.group(9);
    CrawlerDetection crawlerDe = new CrawlerDetection(props);
    if (crawlerDe.checkKnownCrawler(agent)) {
      return lineJson;
    } else {

      String[] mimeTypes = props.getProperty(MudrodConstants.BLACK_LIST_REQUEST).split(",");
      for (String mimeType : mimeTypes) {
        if (request.contains(mimeType)) {
          return lineJson;
        }
      }

      ApacheAccessLog accesslog = new ApacheAccessLog();
      accesslog.LogType = MudrodConstants.HTTP_LOG;
      accesslog.IP = matcher.group(1);
      accesslog.Request = matcher.group(5);
      accesslog.Response = matcher.group(6);
      accesslog.Bytes = Double.parseDouble(bytes);
      accesslog.Referer = matcher.group(8);
      accesslog.Browser = matcher.group(9);
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
      accesslog.Time = df.format(date);

      Gson gson = new Gson();
      lineJson = gson.toJson(accesslog);

      return lineJson;
  }
  }

  public static boolean checknull(WebLog s) {
    if (s == null) {
      return false;
    }
    return true;
  }

}
