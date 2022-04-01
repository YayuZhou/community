package com.zyy.community.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String replace = "**";
    private TireNode rootNode = new TireNode();

    @PostConstruct
    private void init(){
        try (
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                ){
                String keyword;
                while((keyword = reader.readLine()) != null){
                    this.addKeyword(keyword);
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addKeyword(String keyword){
        TireNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            TireNode subNode = tempNode.getTireNode(c);
            if(subNode == null){
                subNode = new TireNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if(i == keyword.length() -1){
                tempNode.setKeywordEnd(true);
            }

        }
    }

    public String filter(String text){
        StringBuilder ans = new StringBuilder();
        TireNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        while(position < text.length()){
            char c = text.charAt(position);
            //跳过符号
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    ans.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getTireNode(c);
            if(tempNode == null){
                ans.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                ans.append(replace);
                begin = ++position;
                tempNode = rootNode;
            }else{
                position++;
            }
        }
        ans.append(text.substring(begin));
        return ans.toString();
    }

    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c > 0x9FFF || c < 0x2E80);
    }


    private class TireNode{

        private boolean isKeywordEnd;

        Map<Character, TireNode> subNode = new HashMap<>();

        public boolean isKeywordEnd(){
            return  isKeywordEnd;
        }

        public void setKeywordEnd(boolean  keywordEnd){
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TireNode node){
            subNode.put(c, node);
        }

        //获取子节点
        public TireNode getTireNode(Character c){
            return subNode.get(c);
        }

    }
}
