package com.erico.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexFiles {
	
	public static String CORPUS_PATH = "C:\\Users\\Jmslord\\Documents\\NetBeansProjects\\customized-indonesian-analyzer\\corpus-example";
	public static String INDEX_PATH = "C:\\Users\\Jmslord\\Documents\\NetBeansProjects\\customized-indonesian-analyzer\\index-example";
	public static final boolean USE_STEMMER = true;
	public static final boolean USE_STOPWORD = true;
        //public static final String CORPUS_PATH = "D:\\Documents\\NetBeansProjects\\lucene\\corpus-example";
        //public static final String INDEX_PATH = "D:\\Documents\\NetBeansProjects\\lucene\\index-example";
	public static void main(String[] args) {
		try {
			Directory indexDir = FSDirectory.open(Paths.get(INDEX_PATH));
			Analyzer analyzer = new CustomizedIndonesianAnalyzer(USE_STEMMER, USE_STOPWORD);
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			config.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDir, config);

			Path docDir = Paths.get(CORPUS_PATH);
			indexDocs(indexWriter, docDir);
			indexWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void indexDocs(final IndexWriter writer, Path path)
			throws IOException {
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) throws IOException {
					try {
						indexDoc(writer, file, attrs.lastModifiedTime()
								.toMillis());
					} catch (IOException ignore) {
						ignore.printStackTrace();
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		}
	}
	static void indexDoc(IndexWriter writer, Path file, long lastModified)
			throws IOException {
		try (InputStream stream = Files.newInputStream(file)) {
			Document doc = new Document();
			Field pathField = new StringField("path", file.toString(),
					Field.Store.YES);
			doc.add(pathField);
			doc.add(new LongField("modified", lastModified, Field.Store.NO));
			doc.add(new TextField("contents", new BufferedReader(
					new InputStreamReader(stream, StandardCharsets.UTF_8))));
			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {	
				System.out.println("adding " + file);
				writer.addDocument(doc);
			} else {
				System.out.println("updating " + file);
				writer.updateDocument(new Term("path", file.toString()), doc);
			}
		}
	}
}
