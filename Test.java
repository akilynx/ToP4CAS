package jp.ac.nagoya_u.is.nakamura.testpackage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import jp.ac.nagoya_u.is.nakamura.diffpackage.Extractor;
import jp.ac.nagoya_u.is.nakamura.diffpackage.LatestData;
import jp.ac.nagoya_u.is.nakamura.diffpackage.Result;
import jp.ac.nagoya_u.is.nakamura.diffpackage.History;
import jp.ac.nagoya_u.is.nakamura.diffpackage.Data;
import jp.ac.nagoya_u.is.nakamura.diffpackage.InsertData;
import jp.ac.nagoya_u.is.nakamura.diffpackage.DeleteData;
import jp.ac.nagoya_u.is.nakamura.diffpackage.LeaveData;
import jp.ac.nagoya_u.is.nakamura.diffpackage.RestoreData;
import jp.ac.nagoya_u.is.nakamura.diffpackage.Term;

public class Test {

	/**
	 * main
	 * @param args
	 */
	public static void main(String[] args) {
		
		Extractor extractor = new Extractor();
		String article = "テスト記事";
		List<History> histories = new ArrayList<>();
		int thread = 8;
		String section = "-";
		String text = "以下 、 テスト 記事 本文 。 \n";
		for(int i = 0; i < 10; i++){
			if(i == 6){
				String[] vandalText = {"全消し"};
				History history =new History("荒らし", vandalText);
				histories.add(history);
			}else{	
				String editor = "編集者" + i%6;
				if(i%4==0){
					text += "\n "+section+" 節 "+i/4+" \n";		
				}
				text += "これは テスト" + i +" です 。 \n";
				
				History history =new History(editor, text.split(" "));
				histories.add(history);
			}
		}
		
		Result result = extractor.extract(article, histories, 0, new ArrayList<Data>(), thread, section, 20, 3, 3, 1, false, true, false);
		/*
		------------------
		入出力の説明
		------------------
		>入力
		String article // 記事名
		List<History> histories // 編集履歴データ
		int version // 初期版番号。標準は0。前のデータをもとにデータ抽出する場合は変更。（リアルタイム抽出用）
		List<Data> data // 初期の版データ。要素数0でよい。（リアルタイム抽出用）
		int thread // スレッド数
		String section // 節の区切り（Wikipediaならば "=="）
		int h // 参照する過去の版数を制限する閾値。推奨値は20
		int k // シーケンスマッチングにおける最小連続単語数の閾値。推奨値は4
		int l // 移動＋復元とみなす最小連続単語数の閾値。推奨値は9。r<=l
		int r // 一が一致する復元とみなす最小連続単語数の閾値。推奨値は1
		boolean comment // コメントを出力するかどうか
		boolean remove // 除去すべきデータ（残存後に同一人物によって削除されたときの残存データ、削除後に同一人物により復元されたときの削除データ）を除去するかどうか
		boolean self // 復元者＝追加者のときに復元データを抽出するかどうか（true: する）
		
		>出力: Return
		String article; // 記事名
		TreeMap<Integer, String> editorsData = new TreeMap<>(); // 版番号、編集者
		HashMap<String, Integer> frequenciesData = new HashMap<>(); // 編集者、編集回数
		List<InsertData> insertsData = new ArrayList<>(); // 追加データ
		List<DeleteData> deletesData = new ArrayList<>(); // 削除データ
		List<RestoreData> restoresData = new ArrayList<>(); // 復元データ
		List<LeaveData> leavesData = new ArrayList<>(); // 残存データ
		LatestData latestData; // 最終版のデータ
		HashMap<Integer, HashMap<String, Integer>> removeLeaveData = new HashMap<>(); // 削除すべき残存データ <version, <inserter, count>>
		HashMap<Integer, HashMap<String, List<String>>> removeDeleteData = new HashMap<>(); // 削除すべき削除データ <version, <inserter, terms>>
		*/
		
		System.out.println("\n■ 記事名：" + result.article);
		System.out.println("\n■ 編集者一覧");
		for(Entry<Integer, String> editorData: result.editorsData.entrySet()){
			System.out.println("版"+editorData.getKey()+": "+editorData.getValue());
		}
		
		System.out.println("\n■ 編集回数一覧");
		for(Entry<String, Integer> frequencyData: result.frequenciesData.entrySet()){
			System.out.println(frequencyData.getKey()+": "+frequencyData.getValue()+"回");
		}
		
		System.out.println("\n■ 最終版のデータ");
		LatestData latestData = result.latestData;
		System.out.println("・本文（最終版："+latestData.version+", 最終編集者："+latestData.editor+"）");
		for(Term term: latestData.terms){
			System.out.print(term.term);
		}
		
		System.out.println("\n・単語ごとのデータ");
		for(Data data: latestData.data){
			if(data.state){ //ON状態の単語だけを表示
				System.out.print(data.term+"\t| 追加> 版"+data.version+":"+data.inserter+" "); // 単語と追加者
				
				if(!data.leavers.isEmpty()){
					System.out.print("| 残存> ");
					for(Entry<String, Integer> leaver: data.leavers.entrySet()){
						System.out.print("版"+leaver.getValue()+":"+leaver.getKey()+" "); // 残存者
					}
				}
				
				if(!data.deleters.isEmpty()){
					System.out.print("| 削除> ");
					for(Entry<Integer, String> deleter: data.deleters.entrySet()){
						System.out.print("版"+deleter.getKey()+":"+deleter.getValue()+" "); // 削除者
					}
				}
				if(!data.restorers.isEmpty()){
					System.out.print("| 復元> ");
					for(Entry<Integer, String> restorer: data.restorers.entrySet()){
						System.out.print("版"+restorer.getKey()+":"+restorer.getValue()+" "); // 復元者
					}
				}
				System.out.println();
			}
		}
		
		int count = 0;
		System.out.println("\n■ 追加のデータ");
		for(InsertData insertData: result.insertsData){
			System.out.println("版"+insertData.version+": "+insertData.editor+" > "+insertData.term);
			count++;
			if(count >= 10){
				count = 0;
				System.out.println("...略");
				break;
			}
		}
		
		System.out.println("\n■ 残存のデータ");
		for(LeaveData leaveData: result.leavesData){
			System.out.println("版"+leaveData.version+": "+leaveData.editor+" > "+leaveData.inserter +" "+ leaveData.count + "単語");
			count++;
			if(count >= 10){
				count = 0;
				System.out.println("...略");
				break;
			}
		}
		
		System.out.println("\n■ 削除のデータ");
		for(DeleteData deleteData: result.deletesData){
			System.out.println("版"+deleteData.version+": "+deleteData.editor+" > "+deleteData.inserter +" "+deleteData.term);
			count++;
			if(count >= 10){
				count = 0;
				System.out.println("...略");
				break;
			}
		}
		
		System.out.println("\n■ 復元のデータ");
		for(RestoreData restoreData: result.restoresData){
			System.out.println("版"+restoreData.version+": "+restoreData.editor+" > "+restoreData.inserter +" "+restoreData.term);
			count++;
			if(count >= 10){
				count = 0;
				System.out.println("...略");
				break;
			}
		}
		
	}

}
