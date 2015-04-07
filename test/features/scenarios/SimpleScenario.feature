#language: ja

フィーチャ: 簡単なシナリオテスト

  シナリオ: 商品登録から注文までの基本的な流れ
    前提    以下のユーザを登録する:
        | メールアドレス   | パスワード   | 姓               | 名         | ロール       |
        | admin@localhost  | adminadmin   | お弁当           | 管理者     | 管理者       |
        | tom@localhost    | tomhoehoe    | ポッペンディーク | トム       | 一般メンバー |
        | mary@localhost   | maryhoehoe   | ポッペンディーク | メアリー   | 一般メンバー |
        | henrik@localhost | henrikhoehoe | クニベルグ       | ヘンリック | 一般メンバー |
      かつ  ギャザリングの設定を以下のようにする:
        | 有効  | 目標件数 | 値引き額 |
        | true  |      30  |      20  |

    # ユーザの確認
    # 姓、名の順で表示されること
    もし    ユーザ "admin@localhost" パスワード "adminadmin" でサインインする
    ならば  ユーザ一覧が以下の内容であること:
        | No. | メールアドレス   | 姓               | 名         | 管理者 |
        | 1   | admin@localhost  | お弁当           | 管理者     | true   |
        | 2   | henrik@localhost | クニベルグ       | ヘンリック | false  |
        | 3   | tom@localhost    | ポッペンディーク | トム       | false  |
        | 4   | mary@localhost   | ポッペンディーク | メアリー   | false  |
    もし    サインアウトする

    # 商品の登録と、メニューの作成
    もし    ユーザ "admin@localhost" パスワード "adminadmin" でサインインする
      かつ  以下の商品を登録する:
        | カテゴリ       | 店名       | レジ番号 | 商品番号 | 商品名       | 注文(定価) | 注文(割引額) | 発注(税抜) | 発注(税込) | 商品コード                                     | ステータス |
        | 弁当           | 銀座魚屋   | 11       | ①       | 鯛茶漬け     | 500        |              | 490        | 529        | 銀座魚屋　① 鯛茶漬け　500円                   | 有効       |
        | 弁当           | 渋谷肉屋   | 12       | ①       | 生姜焼き弁当 | 620        | 20           | 610        | 658        | 渋谷肉屋　① 生姜焼き弁当　620円 - 20円＝600円 | 有効       |
        | 弁当           | 渋谷肉屋   | 12       | ②       | 焼肉弁当     | 700        |              | 690        | 745        | 渋谷肉屋　② 焼肉弁当　700円                   | 有効       |
        | サイドメニュー | 新宿果物屋 | 13       | A        | バナナ       | 160        | 10           | 150        | 162        | 新宿果物屋　A バナナ　160円 - 10円＝150円      | 有効       |
        | サイドメニュー | 新宿果物屋 | 13       | B        | マンゴー     | 300        |              | 290        | 313        | 新宿果物屋　B マンゴー　300円                  | 有効       |
      かつ  以下の内容のメニューを作成する:
        | 日付       | 今週 火曜日                                               |
        | ステータス | 受付中                                                    |
        | 商品-1     | 弁当　銀座魚屋　① 鯛茶漬け　500円                        |
        | 商品-2     | 弁当　渋谷肉屋　① 生姜焼き弁当　620円 - 20円＝600円      |
        | 商品-3     | サイドメニュー　新宿果物屋　A バナナ　160円 - 10円＝150円 |
        | 商品-4     | サイドメニュー　新宿果物屋　B マンゴー　300円             |
      かつ  サインアウトする

    # メアリーが注文
    もし    ユーザ "mary@localhost" パスワード "maryhoehoe" でサインインする
      かつ  以下の内容で注文する:
        | 日付       | 今週 火曜日                                    |
        | 商品-1     | 渋谷肉屋　① 生姜焼き弁当　620円 - 20円＝600円 |
      かつ  サインアウトする

    # トムが注文
    もし    ユーザ "tom@localhost" パスワード "tomhoehoe" でサインインする
      かつ  以下の内容で注文する:
        | 日付       | 今週 火曜日                               |
        | 商品-1     | 銀座魚屋　① 鯛茶漬け　500円              |
        | 商品-2     | 新宿果物屋　A バナナ　160円 - 10円＝150円 |
      かつ  サインアウトする

    # ヘンリックが注文
    もし    ユーザ "henrik@localhost" パスワード "henrikhoehoe" でサインインする
      かつ  以下の内容で注文する:
        | 日付       | 今週 火曜日                               |
        | 商品-1     | 銀座魚屋　① 鯛茶漬け　500円              |
        | 商品-2     | 新宿果物屋　A バナナ　160円 - 10円＝150円 |
      かつ  サインアウトする

    # チェックリストの確認
    # 商品は弁当、サイドメニューの順で表示されていること
    # 氏名は、左側の商品から順に、商品を注文した人ごとに表示されていること
    # 同じ商品を注文した人は、氏名の昇順で表示されていること
    # 注文のない商品は表示されないこと
    # ヘッダと同じ内容のフッタがあること (目視で確認する)
    もし    ユーザ "admin@localhost" パスワード "adminadmin" でサインインする
    ならば  日付 "今週 火曜日" のチェック表の総額が "1,900円" かつ、以下の内容であること:
        | チェック | 氏名                        | 渋谷肉屋　① 生姜焼き弁当　620円 - 20円＝600円 | 銀座魚屋　① 鯛茶漬け　500円 | 新宿果物屋　A バナナ　160円 - 10円＝150円 |
        |          | ポッペンディーク メアリー   | ○                                             |                              |                                           |
        |          | クニベルグ ヘンリック       |                                                | ○                           | ○                                        |
        |          | ポッペンディーク トム       |                                                | ○                           | ○                                        |

    # 発注確認シートの確認
    # 1週間分が表示されていること (未確認 -> 現在は個別に確認している)
    # メニューに登録した弁当がすべて表示されていること (サイドメニューは表示されないこと)
    # レジ番号、ショップ名、Noの順で並んでいること (未確認 -> データが足りない)
    ならば  日付 "今週 月曜日" の発注確認シートにデータがないこと
    ならば  日付 "今週 火曜日" の発注確認シートが以下の内容であること:
        | レジ | ショップ名 | No | 品名         | 税抜 | 税込 |
        | 11   | 銀座魚屋   | ① | 鯛茶漬け     |  490 |  529 |
        | 12   | 渋谷肉屋   | ① | 生姜焼き弁当 |  610 |  658 |
    ならば  日付 "今週 水曜日" の発注確認シートにデータがないこと
    ならば  日付 "今週 木曜日" の発注確認シートにデータがないこと
    ならば  日付 "今週 金曜日" の発注確認シートにデータがないこと

    # 発注シートの確認
    # 1週間分が表示されていること (未確認 -> 現在は個別に確認している)
    # 注文が必要な商品のみ表示されていること
    # レジ番号、ショップ名、Noの順で並んでいること (未確認 -> データが足りない)
    # 送料があること (1個、540円固定)
    # 小計(商品の和)、合計(商品と送料の和)があること
    ならば  日付 "今週 月曜日" の発注シートにデータがないこと
    ならば  日付 "今週 火曜日" の発注シートが以下の内容であること:
        | レジ | ショップ名 | No | 品名         | 税抜 | 税込 | 数量 | 金額 |
        | 11   | 銀座魚屋   | ① | 鯛茶漬け     |  490 |  529 | 2    | 1058 |
        | 12   | 渋谷肉屋   | ① | 生姜焼き弁当 |  610 |  658 | 1    |  658 |
        | 13   | 新宿果物屋 | A  | バナナ       |  150 |  162 | 2    |  324 |
        |      |            |    | 小計         |      |      | 5    | 2040 |
        |      |            |    | 送料         |      |      | 1    |  540 |
        |      |            |    | 合計         |      |      | 6    | 2580 |
    ならば  日付 "今週 水曜日" の発注シートにデータがないこと
    ならば  日付 "今週 木曜日" の発注シートにデータがないこと
    ならば  日付 "今週 金曜日" の発注シートにデータがないこと

    # 入出金管理台帳の確認
    # 1週間分が表示されていること
    # メニューに登録した商品がすべて表示されていること (弁当とサイドメニュー両方)
    # コードと数量が表示されていること
    ならば  日付 "今週 月曜日" の入出金管理台帳にデータがないこと
    ならば  日付 "今週 火曜日" の入出金管理台帳が以下の内容であること:
        | コード                                         | 数量 |
        | 銀座魚屋　① 鯛茶漬け　500円                   | 2    |
        | 渋谷肉屋　① 生姜焼き弁当　620円 - 20円＝600円 | 1    |
        | 新宿果物屋　A バナナ　160円 - 10円＝150円      | 2    |
        | 新宿果物屋　B マンゴー　300円                  | 0    |
    ならば  日付 "今週 水曜日" の入出金管理台帳にデータがないこと
    ならば  日付 "今週 木曜日" の入出金管理台帳にデータがないこと
    ならば  日付 "今週 金曜日" の入出金管理台帳にデータがないこと


    かつ  サインアウトする

  シナリオ: パスワードの初期化
    前提    以下のユーザを登録する:
        | メールアドレス   | パスワード   | 姓               | 名         | ロール       |
        | admin@localhost  | adminadmin   | お弁当           | 管理者     | 管理者       |
        | tom@localhost    | tomhoehoe    | ポッペンディーク | トム       | 一般メンバー |
      かつ  ギャザリングの設定を以下のようにする:
        | 有効  | 目標件数 | 値引き額 |
        | true  |      30  |      20  |

    もし    ユーザ "tom@localhost" パスワード "tomfugafuga" でパスワードの初期化をする
    ならば  ユーザ "tom@localhost" パスワード "tomfugafuga" でサインインできること
      かつ  ユーザ "tom@localhost" パスワード "tomhoehoe" でサインインできないこと
