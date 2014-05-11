// JavaScript - Companion.JSが入っていないIEで、console.logのエラーを出さない - Qiita
// http://qiita.com/Evolutor_web/items/aebeb657a136ba08884e
// windoオブジェクトにconsoleオブジェクトが無い場合
if (!('console' in window)) {

    // windowオブジェクトにconsoleオブジェクトを作成
    window.console = {};

    // 作ったconsoleオブジェクトに更に引数をそのまま返すlogオブジェクトを作成
    window.console.log = function(str){return str;};
}
