import com.sun.jna.platform.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String [ ] args) {

        if (args.length < 1) {
            System.out.println("Wrong number of arguments.");
            System.exit(1);
        }
        else
        {
            String filepath = args[0];
            try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
                String line;
//                List<String> urls = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    String url = "https://" + GetDomain(line);
                    System.out.println("Processing: " + url);
                    ProcessURL(url);
//                    urls.add(url);
                }
//
//                ExecutorService service = Executors.newFixedThreadPool(4);
//                for (final String x : urls) {
//                    service.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                System.out.println("Thread: " + Thread.currentThread().getName() + ". URL: " + x);
//                                ProcessURL(x);
//                            } catch (Exception e) {
//                                System.out.println("Exception " + e);
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//                service.shutdown();
//                try {
//                    service.awaitTermination(10, TimeUnit.MINUTES);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            } catch (Exception e) {
                System.out.println("Exception " + e);
            }
            System.exit(0);
        }

    }

    public static void ProcessURL (String url) {

        String domain;
        String ip;
        String content;
        List<String> scripts = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> links = new ArrayList<>();


        //We need to clear folder C:\Users\Paul\AppData\Local\Temp
        // otherwise Selenium will spent all free space on disk C:
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File("C:\\Users\\Paul\\AppData\\Local\\Temp"));
        } catch (Exception e) {
            System.out.println("Exception: " + e);
//            System.out.println("Cannot clear Temp folder");
//            System.exit(-1);
        }

        try {
            domain = GetDomain(url);
        } catch (Exception e) {
            System.out.println("[Not found]. Exception = \"\" + e + \"\"");
            domain = "[Not found]. Exception = \"" + e + "\"";
        }

        try{
            InetAddress address = InetAddress.getByName(domain);
            ip = address.getHostAddress();
        } catch (Exception e) {
            ip = "[Not found]. Exception = \"" + e + "\"";
        }

        try {
            // Deactivate remaining firefox processes
            try {
                Runtime.getRuntime().exec("taskkill /im firefox.exe");
            } catch (Exception e)
            {
                System.out.println("Exception: " + e);
            }

            FirefoxDriver driver = new FirefoxDriver();
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
            driver.get(url);
            content = driver.getPageSource();
            List<WebElement> list = driver.findElements(By.xpath("//*[@href or @src]"));
            for(WebElement e : list){
                String link = e.getAttribute("href");
                if(null==link)
                    link=e.getAttribute("src");
                links.add(link);
            }
            driver.quit();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            content = "";
            try {
                Runtime.getRuntime().exec("taskkill /im firefox.exe");
            } catch (Exception e2)
            {
                System.out.println("Exception: " + e2);
            }
        }


        Matcher m = Pattern.compile("<\\s*script\\s*.*?>.*?<\\s*/\\s*script>").matcher(content);
        while (m.find()) {
            scripts.add(m.group());
        }


        String text = content.toLowerCase();
        text = ClearText(text);
        keywords = Arrays.asList(text.split("\\s"));

//        System.out.println("url:    " + url);
//        System.out.println("domain: " + domain);
//        System.out.println("ip:     " + ip);
//        System.out.println("content:     " + content);
//        System.out.println("text:     " + text);
//        System.out.println("links:  " + links);
//        System.out.println("scripts:  " + scripts);

        Set<String> hs = new HashSet<>();
        hs.addAll(keywords);
        keywords = new ArrayList<>();
        keywords.addAll(hs);
//        for (String x : keywords)
//            System.out.println(x);
        String prefix = domain.substring(0,2);
        String path = "Z:\\sites\\" + prefix + "\\" + domain + "\\";
        File fp = new File("Z:\\sites\\" + prefix);
        fp.mkdir();

        File f = new File("Z:\\sites\\" + prefix + "\\" + domain);
        boolean bool = f.mkdir();
        if (bool)
            try {
                PrintWriter writer = new PrintWriter(path + "info", "UTF-8");
                writer.println(url);
                writer.println(domain);
                writer.println(ip);
                writer.close();


                writer = new PrintWriter(path + "content", "UTF-8");
                writer.println(content);
                writer.close();

                writer = new PrintWriter(path + "links", "UTF-8");
                for (String x : links)
                    writer.println(x);
                writer.close();

                writer = new PrintWriter(path + "scripts", "UTF-8");
                for (String x : scripts)
                    writer.println(x);
                writer.close();

                writer = new PrintWriter(path + "keywords", "UTF-8");
                for (String x : keywords)
                    writer.println(x);
                writer.close();

            } catch (Exception e) {
                System.out.println("[!] Caugth exception: " + e);
            }
    }

    public static String GetDomain(String url) {

        String[] parts = url.split("//");
        if (parts.length < 2){
            parts = url.split("/");
            return parts[0];
        }
        else {
            parts = parts[1].split("/");
            return parts[0];
        }
    }

    public static String ClearText(String text) {
        text = text.replace("\n", "").replace("\r", "");
        text = text.replaceAll("<script.*?>.*?</script>", " ");
        text = text.replaceAll("<style.*?>.*?</style>", " ");
        text = text.replaceAll("<.*?>", " ");
        text = text.replaceAll("&lt;.*?&gt;", " ");
//        text = text.replaceAll("[!,.?_)(%@#$]", " ");
//        text = text.replaceAll("[:|;/]", " ");
//        text = text.replaceAll("-", " ");
//        text = text.replaceAll("�", " ");
//        text = text.replaceAll("�", " ");
//        text = text.replaceAll("�", " ");
//        text = text.replaceAll("�", " ");
        text = text.replaceAll("\\s+", " ");
        return text;
    }

}











