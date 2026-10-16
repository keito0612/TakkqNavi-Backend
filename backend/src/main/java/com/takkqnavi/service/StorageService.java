package com.takkqnavi.service;

import java.io.InputStream;

public interface StorageService {
    /**
     * ファイルをアップロード
     * @param path ファイルパス
     * @param inputStream ファイルの入力ストリーム
     * @param contentType コンテンツタイプ
     * @return アップロードされたファイルのURL
     */
    String upload(String path, InputStream inputStream, String contentType);

    /**
     * ファイルを削除
     * @param path ファイルパス
     */
    void delete(String path);

    /**
     * ファイルのURLを取得
     * @param path ファイルパス
     * @return ファイルのURL
     */
    String getUrl(String path);

    /**
     * URLからファイルパスを抽出
     * @param url ファイルのURL
     * @return ファイルパス（URLがnullまたは空の場合はnull）
     */
    String extractPath(String url);
}
