package com.selenium.studio.ui;

public class HtmlBuilder {

    public static String build() {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Selenium Automation Studio v2.0</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&family=DM+Sans:wght@300;400;500;600;700&display=swap' rel='stylesheet'>" +
            buildCss() +
            "</head><body>" +
            buildHeader() +
            "<div class='layout'>" +
            buildSidebar() +
            buildMain() +
            buildRightPanel() +
            "</div>" +
            buildCodeModal() +
            "<div class='toast' id='toast'></div>" +
            buildScript() +
            "</body></html>";
    }

    private static String buildCss() {
        return "<style>" +
            ":root{--bg:#0a0c12;--s1:#111420;--s2:#181c2a;--s3:#1e2234;--bd:#252940;--bd2:#303558;" +
            "--acc:#4f8ef7;--acc2:#7c5ce4;--grn:#00e5a0;--red:#ff5270;--yel:#ffca3a;--ora:#ff9f43;" +
            "--tx:#dce1f0;--tx2:#8890aa;--tx3:#4a5070;--mo:'JetBrains Mono',monospace;--sa:'DM Sans',sans-serif;" +
            "--glow-b:0 0 18px rgba(79,142,247,.25);--glow-g:0 0 18px rgba(0,229,160,.25);--glow-r:0 0 18px rgba(255,82,112,.25);}" +
            "*{margin:0;padding:0;box-sizing:border-box;}" +
            "body{font-family:var(--sa);background:var(--bg);color:var(--tx);height:100vh;display:flex;flex-direction:column;overflow:hidden;}" +
            "::-webkit-scrollbar{width:4px;height:4px;}" +
            "::-webkit-scrollbar-track{background:transparent;}" +
            "::-webkit-scrollbar-thumb{background:var(--bd2);border-radius:4px;}" +
            ".hdr{background:var(--s1);border-bottom:1px solid var(--bd);padding:0 18px;height:52px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".logo{display:flex;align-items:center;gap:10px;}" +
            ".logo-icon{width:28px;height:28px;background:linear-gradient(135deg,var(--acc),var(--acc2));border-radius:7px;display:flex;align-items:center;justify-content:center;font-size:14px;}" +
            ".logo-txt{font-size:15px;font-weight:600;}" +
            ".logo-ver{font-size:10px;background:linear-gradient(135deg,var(--acc),var(--acc2));-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-family:var(--mo);font-weight:600;}" +
            ".ha{display:flex;gap:6px;align-items:center;}" +
            ".btn{padding:6px 13px;border-radius:7px;border:1px solid var(--bd2);background:transparent;color:var(--tx2);font-family:var(--sa);font-size:12px;font-weight:500;cursor:pointer;transition:all .15s;display:inline-flex;align-items:center;gap:5px;white-space:nowrap;}" +
            ".btn:hover{background:var(--s3);border-color:var(--acc);color:var(--acc);}" +
            ".btn-run{background:var(--grn);border-color:var(--grn);color:#000;font-weight:700;box-shadow:var(--glow-g);}" +
            ".btn-run:hover{background:#00d490;box-shadow:0 0 24px rgba(0,229,160,.4);color:#000;}" +
            ".btn-stop{background:var(--red);border-color:var(--red);color:#fff;display:none;box-shadow:var(--glow-r);}" +
            ".btn-stop:hover{background:#e0405f;color:#fff;}" +
            ".btn-sm{padding:4px 9px;font-size:11px;}" +
            ".btn-acc{background:rgba(79,142,247,.15);border-color:var(--acc);color:var(--acc);}" +
            ".btn-acc:hover{background:rgba(79,142,247,.25);}" +
            ".btn-grn{background:rgba(0,229,160,.12);border-color:var(--grn);color:var(--grn);}" +
            ".btn-grn:hover{background:rgba(0,229,160,.22);}" +
            ".btn-red{border-color:var(--red);color:var(--red);}" +
            ".btn-red:hover{background:rgba(255,82,112,.1);}" +
            ".layout{display:flex;flex:1;overflow:hidden;}" +
            ".sidebar{width:268px;min-width:268px;background:var(--s1);border-right:1px solid var(--bd);overflow-y:auto;display:flex;flex-direction:column;}" +
            ".main{flex:1;display:flex;flex-direction:column;overflow:hidden;}" +
            ".rp{width:320px;min-width:320px;background:var(--s1);border-left:1px solid var(--bd);display:flex;flex-direction:column;}" +
            ".ph{padding:10px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".pt{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--tx3);}" +
            ".sec{padding:12px 13px;border-bottom:1px solid var(--bd);}" +
            ".sl{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx3);margin-bottom:8px;}" +
            ".fl{margin-bottom:9px;}.fl:last-child{margin-bottom:0;}" +
            ".fl label{font-size:11px;color:var(--tx2);display:block;margin-bottom:4px;font-weight:500;}" +
            "input[type=text],input[type=number],select,textarea{width:100%;background:var(--s2);border:1px solid var(--bd2);color:var(--tx);border-radius:6px;padding:6px 9px;font-family:var(--sa);font-size:12px;outline:none;transition:border .15s;}" +
            "input:focus,select:focus{border-color:var(--acc);box-shadow:0 0 0 2px rgba(79,142,247,.1);}" +
            "select option{background:var(--s2);}" +
            ".bg{display:grid;grid-template-columns:1fr 1fr;gap:6px;}" +
            ".bc{background:var(--s2);border:1px solid var(--bd2);border-radius:7px;padding:8px;cursor:pointer;transition:all .15s;display:flex;align-items:center;gap:7px;user-select:none;}" +
            ".bc:hover{border-color:var(--acc);}" +
            ".bc.on{border-color:var(--acc);background:rgba(79,142,247,.08);box-shadow:inset 0 0 0 1px rgba(79,142,247,.3);}" +
            ".bi{font-size:17px;}.bn{font-size:11px;font-weight:600;}" +
            ".cd{width:12px;height:12px;border-radius:50%;border:2px solid var(--bd2);margin-left:auto;transition:all .15s;flex-shrink:0;}" +
            ".bc.on .cd{background:var(--acc);border-color:var(--acc);box-shadow:0 0 6px var(--acc);}" +
            ".tr{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;}" +
            ".tr:last-child{margin-bottom:0;}" +
            ".tl{font-size:12px;color:var(--tx2);display:flex;align-items:center;gap:5px;}" +
            ".tg{position:relative;width:32px;height:17px;}" +
            ".tg input{display:none;}" +
            ".ts{position:absolute;inset:0;background:var(--bd2);border-radius:20px;cursor:pointer;transition:.2s;}" +
            ".ts::after{content:'';position:absolute;width:11px;height:11px;left:3px;top:3px;background:#fff;border-radius:50%;transition:.2s;}" +
            ".tg input:checked+.ts{background:var(--acc);}" +
            ".tg input:checked+.ts::after{transform:translateX(15px);}" +
            ".tg-api input:checked+.ts{background:var(--ora);}" +
            ".rr{display:flex;gap:6px;align-items:center;}.rr span{color:var(--tx3);font-size:11px;}.rr input{width:70px;}" +
            ".r2{display:grid;grid-template-columns:1fr 1fr;gap:7px;}" +
            ".stb{padding:9px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;background:var(--s1);flex-shrink:0;}" +
            ".sc{flex:1;overflow-y:auto;padding:9px;background:var(--bg);}" +
            ".sk{background:var(--s1);border:1px solid var(--bd);border-radius:9px;margin-bottom:8px;overflow:hidden;transition:border .2s;}" +
            ".sk:hover{border-color:var(--bd2);}" +
            ".sk.pass{border-left:3px solid var(--grn);}.sk.fail{border-left:3px solid var(--red);}.sk.run{border-left:3px solid var(--yel);animation:pulse 1s infinite;}" +
            "@keyframes pulse{0%,100%{opacity:1}50%{opacity:.65}}" +
            ".sh{padding:8px 11px;display:flex;align-items:center;gap:8px;cursor:pointer;}" +
            ".sn{width:20px;height:20px;border-radius:5px;background:var(--s3);display:flex;align-items:center;justify-content:center;font-size:9px;font-weight:700;font-family:var(--mo);color:var(--tx3);flex-shrink:0;}" +
            ".sk.pass .sn{background:rgba(0,229,160,.12);color:var(--grn);}.sk.fail .sn{background:rgba(255,82,112,.12);color:var(--red);}.sk.run .sn{background:rgba(255,202,58,.12);color:var(--yel);}" +
            ".sab{font-size:9px;font-weight:700;font-family:var(--mo);padding:2px 6px;border-radius:4px;background:rgba(79,142,247,.12);color:var(--acc);flex-shrink:0;text-transform:uppercase;letter-spacing:.5px;}" +
            ".ss{flex:1;font-size:11px;color:var(--tx2);overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}" +
            ".sb{padding:10px 11px;border-top:1px solid var(--bd);background:rgba(0,0,0,.2);display:none;}" +
            ".sb.open{display:block;}" +
            ".sf{display:grid;gap:7px;}" +
            ".war{display:flex;align-items:center;gap:7px;margin-top:7px;}" +
            ".war label{font-size:11px;color:var(--tx3);white-space:nowrap;}.war input{width:70px;}" +
            ".sar{display:flex;gap:6px;margin-top:9px;align-items:center;}" +
            ".sar .save-ok{font-size:11px;color:var(--grn);display:none;}" +
            ".asa{padding:10px 13px;border-top:1px solid var(--bd);background:var(--s1);display:none;flex-shrink:0;}" +
            ".ag{display:grid;grid-template-columns:repeat(3,1fr);gap:6px;margin-top:8px;}" +
            ".at{background:var(--s2);border:1px solid var(--bd);border-radius:7px;padding:8px 6px;text-align:center;cursor:pointer;transition:all .15s;}" +
            ".at:hover{border-color:var(--acc);background:rgba(79,142,247,.08);transform:translateY(-1px);}" +
            ".at .ic{font-size:17px;margin-bottom:2px;}.at .nm{font-size:9px;font-weight:600;color:var(--tx2);line-height:1.3;text-transform:uppercase;letter-spacing:.4px;}" +
            ".lp{flex:1;overflow-y:auto;padding:8px;font-family:var(--mo);font-size:10.5px;}" +
            ".ll{padding:2px 0;display:flex;gap:7px;border-bottom:1px solid rgba(37,41,64,.5);line-height:1.5;}" +
            ".lt{color:var(--tx3);flex-shrink:0;min-width:57px;}.lm{word-break:break-all;}" +
            ".lm.INFO{color:var(--tx2);}.lm.PASS{color:var(--grn);}.lm.FAIL{color:var(--red);}.lm.WARN{color:var(--yel);}.lm.REQ{color:#7cd4f7;}.lm.RES{color:#c3a6ff;}" +
            ".pbw{background:var(--bg);border-radius:4px;height:3px;margin:5px 13px;overflow:hidden;flex-shrink:0;}" +
            ".pb{height:100%;background:linear-gradient(90deg,var(--acc),var(--grn));border-radius:4px;transition:width .3s;width:0%;}" +
            ".sr{display:flex;gap:6px;padding:8px 13px;border-bottom:1px solid var(--bd);flex-shrink:0;}" +
            ".sc2{flex:1;background:var(--s2);border-radius:7px;padding:7px;text-align:center;border:1px solid var(--bd);}" +
            ".sn2{font-size:17px;font-weight:700;font-family:var(--mo);}.sl2{font-size:9px;color:var(--tx3);text-transform:uppercase;letter-spacing:.5px;margin-top:1px;}" +
            ".sn2.g{color:var(--grn);}.sn2.r{color:var(--red);}.sn2.y{color:var(--yel);}" +
            ".es{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:var(--tx3);text-align:center;padding:30px;}" +
            ".es .ei{font-size:38px;margin-bottom:10px;opacity:.3;}.es p{font-size:12px;line-height:1.7;}" +
            ".toast{position:fixed;bottom:18px;right:18px;background:var(--s3);border:1px solid var(--bd2);border-radius:9px;padding:9px 15px;font-size:12px;z-index:999;transform:translateY(60px);opacity:0;transition:all .3s;}" +
            ".toast.show{transform:translateY(0);opacity:1;}.toast.ok{border-color:var(--grn);color:var(--grn);background:rgba(0,229,160,.08);}.toast.er{border-color:var(--red);color:var(--red);background:rgba(255,82,112,.08);}" +
            ".mo{position:fixed;inset:0;background:rgba(0,0,0,.75);z-index:200;display:none;align-items:center;justify-content:center;backdrop-filter:blur(4px);}" +
            ".mo.open{display:flex;}" +
            ".md{background:var(--s1);border:1px solid var(--bd2);border-radius:13px;width:580px;max-height:86vh;overflow-y:auto;box-shadow:0 24px 60px rgba(0,0,0,.6);}" +
            ".mh{padding:16px 20px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;}" +
            ".mt{font-size:14px;font-weight:600;}.mc{background:none;border:none;color:var(--tx3);cursor:pointer;font-size:17px;line-height:1;}.mc:hover{color:var(--tx);}" +
            ".mb{padding:16px 20px;}.mf{padding:12px 20px;border-top:1px solid var(--bd);display:flex;gap:8px;justify-content:flex-end;}" +
            ".ca{background:var(--bg);border:1px solid var(--bd);border-radius:7px;padding:13px;font-family:var(--mo);font-size:10.5px;color:#8be0a4;line-height:1.7;overflow-x:auto;white-space:pre;max-height:420px;overflow-y:auto;}" +
            ".tabs{display:flex;border-bottom:1px solid var(--bd);flex-shrink:0;}" +
            ".tab{padding:8px 14px;font-size:11px;font-weight:600;color:var(--tx3);cursor:pointer;border-bottom:2px solid transparent;margin-bottom:-1px;transition:all .15s;display:flex;align-items:center;gap:5px;}" +
            ".tab:hover{color:var(--tx2);}.tab.active{color:var(--acc);border-bottom-color:var(--acc);}" +
            ".tab-pane{display:none;flex:1;overflow:hidden;flex-direction:column;}.tab-pane.active{display:flex;}" +
            ".api-badge{font-size:9px;padding:1px 5px;border-radius:3px;background:rgba(255,159,67,.15);color:var(--ora);font-family:var(--mo);}" +
            ".api-live{display:inline-block;width:6px;height:6px;border-radius:50%;background:var(--ora);margin-right:3px;animation:blink 1s infinite;}" +
            "@keyframes blink{0%,100%{opacity:1}50%{opacity:.2}}" +
            ".tag{font-size:10px;padding:2px 7px;border-radius:20px;}.tb{background:rgba(79,142,247,.12);color:var(--acc);}" +
            ".ltb{padding:6px 9px;border-bottom:1px solid var(--bd);display:flex;gap:5px;align-items:center;flex-shrink:0;}" +
            "</style>";
    }

    private static String buildHeader() {
        return "<div class='hdr'>" +
            "<div class='logo'>" +
            "<div class='logo-icon'>&#x1F916;</div>" +
            "<div class='logo-txt'>Selenium Studio</div>" +
            "<div class='logo-ver'>v2.0</div>" +
            "</div>" +
            "<div class='ha'>" +
            "<button class='btn btn-sm' id='btnSave'>&#x1F4BE; Save</button>" +
            "<button class='btn btn-sm' id='btnLoad'>&#x1F4C2; Load</button>" +
            "<button class='btn btn-sm' id='btnCode'>&#x3C;/&#x3E; Code</button>" +
            "<div style='width:1px;height:22px;background:var(--bd);margin:0 2px;'></div>" +
            "<button class='btn btn-run' id='btnRun'>&#x25B6; Run Tests</button>" +
            "<button class='btn btn-stop' id='btnStop'>&#x23F9; Stop</button>" +
            "</div></div>";
    }

    private static String buildSidebar() {
        return "<div class='sidebar'>" +
            "<div class='ph'><span class='pt'>&#x2699;&#xFE0F; Configuration</span></div>" +
            "<div class='sec'><div class='sl'>Target URL</div>" +
            "<div class='fl'><input type='text' id='url' placeholder='https://example.com'/></div></div>" +
            "<div class='sec'><div class='sl'>Browser</div><div class='bg'>" +
            "<div class='bc' id='bc-chrome' data-b='chrome'><div class='bi'>&#x1F310;</div><div class='bn'>Chrome</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-firefox' data-b='firefox'><div class='bi'>&#x1F98A;</div><div class='bn'>Firefox</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-edge' data-b='edge'><div class='bi'>&#x1F535;</div><div class='bn'>Edge</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-all' data-b='all'><div class='bi'>&#x26A1;</div><div class='bn'>All</div><div class='cd'></div></div>" +
            "</div></div>" +
            "<div class='sec'><div class='sl'>Window</div>" +
            "<div class='tr'><span class='tl'>&#x1F5A5; Maximize</span><label class='tg'><input type='checkbox' id='maximize' checked><span class='ts'></span></label></div>" +
            "<div class='tr'><span class='tl'>&#x1F47B; Headless Mode</span><label class='tg'><input type='checkbox' id='headless'><span class='ts'></span></label></div>" +
            "<div class='fl' style='margin-top:6px;'><label>Resolution (if not maximized)</label>" +
            "<div class='rr'><input type='number' id='resW' value='1920'/><span>&#xD7;</span><input type='number' id='resH' value='1080'/></div></div></div>" +
            "<div class='sec'><div class='sl'>Timeouts</div><div class='r2'>" +
            "<div class='fl'><label>Implicit (sec)</label><input type='number' id='implWait' value='10'/></div>" +
            "<div class='fl'><label>Page Load (sec)</label><input type='number' id='pageLoad' value='30'/></div>" +
            "</div></div>" +
            "<div class='sec'><div class='sl'>Screenshot</div>" +
            "<div class='tr'><span class='tl'>&#x1F4F8; Auto on Fail</span><label class='tg'><input type='checkbox' id='autoSS' checked><span class='ts'></span></label></div>" +
            "<div class='fl' style='margin-top:6px;'><label>Save Folder</label><input type='text' id='ssDir' value='./screenshots'/></div></div>" +
            "<div class='sec'><div class='sl'>API Capture</div>" +
            "<div class='tr'><span class='tl'><span id='apiLiveInd'></span>&#x1F4E1; Capture Network APIs</span>" +
            "<label class='tg tg-api'><input type='checkbox' id='apiCapture'><span class='ts'></span></label></div>" +
            "<div style='font-size:10px;color:var(--tx3);margin-top:4px;line-height:1.5;'>Chrome only. Logs all HTTP requests &amp; responses during test run.</div>" +
            "</div></div>";
    }

    private static String buildMain() {
        return "<div class='main'>" +
            "<div class='stb'>" +
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<span style='font-size:13px;font-weight:600;'>Test Steps</span>" +
            "<span class='tag tb' id='scnt'>0 steps</span></div>" +
            "<div style='display:flex;gap:6px;'>" +
            "<button class='btn btn-sm' id='btnClear'>&#x1F5D1; Clear All</button>" +
            "<button class='btn btn-sm btn-acc' id='btnAddStep'>&#xFF0B; Add Step</button>" +
            "</div></div>" +
            "<div class='sc' id='sc'>" +
            "<div class='es' id='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to get started.</p></div>" +
            "</div>" +
            "<div class='asa' id='asa'>" +
            "<div style='display:flex;align-items:center;justify-content:space-between;'>" +
            "<span style='font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx2);'>Choose Action</span>" +
            "<button class='btn btn-sm' id='btnCloseAdd'>&#x2715;</button></div>" +
            "<div class='ag' id='actionGrid'>" +
            "<div class='at' data-action='click'><div class='ic'>&#x1F446;</div><div class='nm'>Click</div></div>" +
            "<div class='at' data-action='type'><div class='ic'>&#x2328;&#xFE0F;</div><div class='nm'>Type Text</div></div>" +
            "<div class='at' data-action='dropdown'><div class='ic'>&#x1F4CB;</div><div class='nm'>Dropdown</div></div>" +
            "<div class='at' data-action='getText'><div class='ic'>&#x1F4D6;</div><div class='nm'>Get Text</div></div>" +
            "<div class='at' data-action='getAttribute'><div class='ic'>&#x1F3F7;&#xFE0F;</div><div class='nm'>Get Attr</div></div>" +
            "<div class='at' data-action='verifyText'><div class='ic'>&#x2705;</div><div class='nm'>Verify Text</div></div>" +
            "<div class='at' data-action='verifyElement'><div class='ic'>&#x1F50D;</div><div class='nm'>Verify Elem</div></div>" +
            "<div class='at' data-action='navigate'><div class='ic'>&#x1F310;</div><div class='nm'>Navigate</div></div>" +
            "<div class='at' data-action='screenshot'><div class='ic'>&#x1F4F8;</div><div class='nm'>Screenshot</div></div>" +
            "<div class='at' data-action='scrollTo'><div class='ic'>&#x2195;&#xFE0F;</div><div class='nm'>Scroll To</div></div>" +
            "<div class='at' data-action='hover'><div class='ic'>&#x1F5B1;</div><div class='nm'>Hover</div></div>" +
            "<div class='at' data-action='clearField'><div class='ic'>&#x1F5D1;&#xFE0F;</div><div class='nm'>Clear Field</div></div>" +
            "<div class='at' data-action='jsClick'><div class='ic'>&#x26A1;</div><div class='nm'>JS Click</div></div>" +
            "<div class='at' data-action='acceptAlert'><div class='ic'>&#x1F514;</div><div class='nm'>Alert</div></div>" +
            "<div class='at' data-action='switchFrame'><div class='ic'>&#x1F5BC;&#xFE0F;</div><div class='nm'>Switch Frame</div></div>" +
            "<div class='at' data-action='switchWindow'><div class='ic'>&#x1F500;</div><div class='nm'>New Window</div></div>" +
            "<div class='at' data-action='print'><div class='ic'>&#x1F4DD;</div><div class='nm'>Print Log</div></div>" +
            "<div class='at' data-action='wait'><div class='ic'>&#x23F3;</div><div class='nm'>Wait</div></div>" +
            "</div></div></div>";
    }

    private static String buildRightPanel() {
        return "<div class='rp'>" +
            "<div class='ph'><span class='pt'>&#x1F4CA; Results &amp; Logs</span></div>" +
            "<div class='sr'>" +
            "<div class='sc2'><div class='sn2 y' id='st'>0</div><div class='sl2'>Total</div></div>" +
            "<div class='sc2'><div class='sn2 g' id='sp'>0</div><div class='sl2'>Pass</div></div>" +
            "<div class='sc2'><div class='sn2 r' id='sf'>0</div><div class='sl2'>Fail</div></div>" +
            "</div>" +
            "<div class='pbw'><div class='pb' id='pb'></div></div>" +
            "<div class='tabs'>" +
            "<div class='tab active' id='tab-exec' data-tab='exec'>&#x1F4DC; Exec Log</div>" +
            "<div class='tab' id='tab-api' data-tab='api'>&#x1F4E1; API Log <span class='api-badge' id='apiCnt'>0</span></div>" +
            "</div>" +
            "<div class='tab-pane active' id='pane-exec'>" +
            "<div class='ltb'>" +
            "<button class='btn btn-sm' id='btnClearLogs'>&#x1F5D1; Clear</button>" +
            "<button class='btn btn-sm btn-grn' id='btnExportLogs'>&#x1F4E4; Export</button>" +
            "</div>" +
            "<div class='lp' id='lp'><div style='color:var(--tx3);font-size:11px;padding:14px;text-align:center;'>Ready...</div></div>" +
            "</div>" +
            "<div class='tab-pane' id='pane-api'>" +
            "<div class='ltb'>" +
            "<button class='btn btn-sm' id='btnClearApiLogs'>&#x1F5D1; Clear</button>" +
            "<button class='btn btn-sm btn-grn' id='btnExportApiLogs'>&#x1F4E4; Export</button>" +
            "<button class='btn btn-sm' style='border-color:var(--ora);color:var(--ora);' id='btnExportApiJson'>JSON</button>" +
            "</div>" +
            "<div class='lp' id='alp'><div style='color:var(--tx3);font-size:11px;padding:14px;text-align:center;'>Enable API Capture in sidebar to start logging network requests.</div></div>" +
            "</div></div>";
    }

    private static String buildCodeModal() {
        return "<div class='mo' id='codeModal'>" +
            "<div class='md' style='width:620px;'>" +
            "<div class='mh'><span class='mt'>&#x3C;/&#x3E; Generated Java Code</span>" +
            "<button class='mc' id='btnCloseCode'>&#x2715;</button></div>" +
            "<div class='mb'>" +
            "<div style='font-size:11px;color:var(--tx2);margin-bottom:10px;'>Copy and paste into Eclipse / IntelliJ as <b>GeneratedTest.java</b>.</div>" +
            "<div class='ca' id='codeDisp'></div>" +
            "</div>" +
            "<div class='mf'>" +
            "<button class='btn' id='btnCopyCode'>&#x1F4CB; Copy All</button>" +
            "<button class='btn btn-acc' id='btnCloseCode2'>Done</button>" +
            "</div></div></div>";
    }

    private static String buildScript() {
        return "<script>\n" +
            "var steps = [], ctr = 0, polling = null, apiPolling = null;\n" +
            "var LABELS = {click:'Click',type:'Type Text',dropdown:'Dropdown',getText:'Get Text',\n" +
            "  getAttribute:'Get Attr',verifyText:'Verify Text',verifyElement:'Verify Elem',\n" +
            "  navigate:'Navigate',screenshot:'Screenshot',scrollTo:'Scroll To',hover:'Hover',\n" +
            "  clearField:'Clear Field',jsClick:'JS Click',acceptAlert:'Alert',\n" +
            "  switchFrame:'Switch Frame',switchWindow:'Switch Window',print:'Print Log',wait:'Wait'};\n" +
            "document.querySelectorAll('.tab').forEach(function(tab){\n" +
            "  tab.addEventListener('click',function(){\n" +
            "    var t=tab.dataset.tab;\n" +
            "    document.querySelectorAll('.tab').forEach(function(x){x.classList.remove('active');});\n" +
            "    document.querySelectorAll('.tab-pane').forEach(function(x){x.classList.remove('active');});\n" +
            "    tab.classList.add('active');\n" +
            "    document.getElementById('pane-'+t).classList.add('active');\n" +
            "  });\n" +
            "});\n" +
            "document.querySelectorAll('.bc').forEach(function(el){\n" +
            "  el.addEventListener('click',function(){\n" +
            "    var b=el.dataset.b;\n" +
            "    if(b==='all'){\n" +
            "      var willOn=!el.classList.contains('on');\n" +
            "      ['chrome','firefox','edge'].forEach(function(x){\n" +
            "        var c=document.getElementById('bc-'+x);\n" +
            "        willOn?c.classList.add('on'):c.classList.remove('on');\n" +
            "      });\n" +
            "      willOn?el.classList.add('on'):el.classList.remove('on');\n" +
            "    } else {\n" +
            "      el.classList.toggle('on');\n" +
            "      var sel=['chrome','firefox','edge'].filter(function(x){return document.getElementById('bc-'+x).classList.contains('on');});\n" +
            "      sel.length===3?document.getElementById('bc-all').classList.add('on'):document.getElementById('bc-all').classList.remove('on');\n" +
            "    }\n" +
            "  });\n" +
            "});\n" +
            "document.getElementById('apiCapture').addEventListener('change',function(){\n" +
            "  var enabled=this.checked;\n" +
            "  fetch('/setapicapture',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({enabled:enabled})});\n" +
            "  var ind=document.getElementById('apiLiveInd');\n" +
            "  ind.innerHTML=enabled?'<span class=\"api-live\"></span>':'';\n" +
            "  toast(enabled?'\\uD83D\\uDCE1 API Capture ON':'API Capture OFF',enabled?'ok':'');\n" +
            "});\n" +
            "document.getElementById('btnAddStep').addEventListener('click',function(){\n" +
            "  var asa=document.getElementById('asa');\n" +
            "  asa.style.display=(asa.style.display==='none'||asa.style.display==='')?'block':'none';\n" +
            "});\n" +
            "document.getElementById('btnCloseAdd').addEventListener('click',function(){document.getElementById('asa').style.display='none';});\n" +
            "document.getElementById('actionGrid').addEventListener('click',function(e){\n" +
            "  var at=e.target.closest('.at'); if(!at) return;\n" +
            "  var action=at.dataset.action; if(!action) return;\n" +
            "  ctr++;\n" +
            "  var newStep={id:ctr,action:action,xpath:'',value:'',value2:'',wait:0,status:'pending'};\n" +
            "  steps.push(newStep); renderSteps(); updateCnt();\n" +
            "  var newSb=document.getElementById('sb-'+ctr);\n" +
            "  if(newSb) newSb.classList.add('open');\n" +
            "  document.getElementById('asa').style.display='none';\n" +
            "  var newSk=document.getElementById('sk-'+ctr);\n" +
            "  if(newSk) newSk.scrollIntoView({behavior:'smooth',block:'nearest'});\n" +
            "  toast('Step added: '+LABELS[action],'ok');\n" +
            "});\n" +
            "document.getElementById('btnClear').addEventListener('click',function(){\n" +
            "  if(steps.length&&!confirm('Delete all steps?')) return;\n" +
            "  steps=[]; renderSteps(); updateCnt();\n" +
            "});\n" +
            "document.getElementById('sc').addEventListener('click',function(e){\n" +
            "  var sh=e.target.closest('.sh');\n" +
            "  var btn=e.target.closest('button[data-act]');\n" +
            "  if(sh&&!btn){var sk=sh.closest('.sk');if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.toggle('open');}return;}\n" +
            "  if(!btn) return;\n" +
            "  var act=btn.dataset.act, id=parseInt(btn.dataset.id);\n" +
            "  if(act==='del'){steps=steps.filter(function(s){return s.id!==id;});renderSteps();updateCnt();}\n" +
            "  else if(act==='up'||act==='dn'){\n" +
            "    var idx=steps.findIndex(function(s){return s.id===id;}),ni=act==='up'?idx-1:idx+1;\n" +
            "    if(ni>=0&&ni<steps.length){var tmp=steps[idx];steps[idx]=steps[ni];steps[ni]=tmp;}\n" +
            "    renderSteps();\n" +
            "    var sk2=document.getElementById('sk-'+id);\n" +
            "    if(sk2){var sb2=sk2.querySelector('.sb');if(sb2)sb2.classList.add('open');}\n" +
            "  } else if(act==='save'){\n" +
            "    var sk3=document.getElementById('sk-'+id);\n" +
            "    if(sk3){var sb3=sk3.querySelector('.sb');if(sb3)sb3.classList.remove('open');\n" +
            "      var saveOk=sk3.querySelector('.save-ok');\n" +
            "      if(saveOk){saveOk.style.display='inline';setTimeout(function(){saveOk.style.display='none';},1800);}}\n" +
            "    toast('Step saved \\u2713','ok');\n" +
            "  }\n" +
            "});\n" +
            "function bindFieldUpdate(evtName){\n" +
            "  document.getElementById('sc').addEventListener(evtName,function(e){\n" +
            "    var el=e.target,id=parseInt(el.dataset.sid),fld=el.dataset.fld;\n" +
            "    if(!id||!fld) return;\n" +
            "    var s=steps.find(function(x){return x.id===id;}); if(!s) return;\n" +
            "    s[fld]=el.value;\n" +
            "    if(fld==='value'&&s.action==='dropdown'){var openId=id;renderSteps();\n" +
            "      var sb=document.getElementById('sb-'+openId);if(sb)sb.classList.add('open');}\n" +
            "    var sk=document.getElementById('sk-'+id);\n" +
            "    if(sk){var ss=sk.querySelector('.ss');if(ss)ss.textContent=s.xpath||s.value||'Configure...';}\n" +
            "  });\n" +
            "}\n" +
            "bindFieldUpdate('change'); bindFieldUpdate('input');\n" +
            "function renderSteps(){\n" +
            "  var c=document.getElementById('sc');\n" +
            "  if(!steps.length){c.innerHTML=\"<div class='es'><div class='ei'>&#x1F4CB;</div><p>No steps yet.<br>Click <b>+ Add Step</b> to get started.</p></div>\";return;}\n" +
            "  var h='';\n" +
            "  steps.forEach(function(s,i){\n" +
            "    var ico=s.status==='pass'?'&#x2705;':s.status==='fail'?'&#x274C;':s.status==='run'?'&#x23F3;':'';\n" +
            "    h+=\"<div class='sk \"+s.status+\"' id='sk-\"+s.id+\"'>\";\n" +
            "    h+=\"<div class='sh'><div class='sn'>\"+(i+1)+\"</div>\";\n" +
            "    h+=\"<div class='sab'>\"+(LABELS[s.action]||s.action)+\"</div>\";\n" +
            "    h+=\"<div class='ss'>\"+escH(s.xpath||s.value||'Configure...')+\"</div>\";\n" +
            "    h+=\"<div style='font-size:15px;margin-left:4px;flex-shrink:0;'>\"+ico+\"</div></div>\";\n" +
            "    h+=\"<div class='sb' id='sb-\"+s.id+\"'>\"+renderFields(s)+\"</div></div>\";\n" +
            "  });\n" +
            "  c.innerHTML=h;\n" +
            "}\n" +
            "function escH(str){return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');}\n" +
            "function inp(sid,fld,val,ph){return \"<input type='text' data-sid='\"+sid+\"' data-fld='\"+fld+\"' value='\"+escH(val)+\"' placeholder='\"+escH(ph)+\"'/>\";}\n" +
            "function numInp(sid,fld,val,ph){return \"<input type='number' data-sid='\"+sid+\"' data-fld='\"+fld+\"' value='\"+escH(String(val))+\"' placeholder='\"+escH(ph)+\"'/>\";}\n" +
            "function renderFields(s){\n" +
            "  var h=\"<div class='sf'>\",a=s.action;\n" +
            "  if(a==='navigate'){h+=\"<div><label>URL to Navigate</label>\"+inp(s.id,'value',s.value,'https://...')+\"</div>\";}\n" +
            "  else if(a==='print'){h+=\"<div><label>Log Message</label>\"+inp(s.id,'value',s.value,'Message to print')+\"</div>\";}\n" +
            "  else if(a==='wait'){h+=\"<div><label>Wait Duration (ms)</label>\"+numInp(s.id,'value',s.value||1000,'1000')+\"</div>\";}\n" +
            "  else if(a==='acceptAlert'){h+=\"<div><label>Action</label><select data-sid='\"+s.id+\"' data-fld='value'>\";\n" +
            "    h+=\"<option value='accept'\"+(s.value!=='dismiss'?' selected':'')+\">Accept (OK)</option>\";\n" +
            "    h+=\"<option value='dismiss'\"+(s.value==='dismiss'?' selected':'')+\">Dismiss (Cancel)</option>\"+\"</select></div>\";}\n" +
            "  else if(a==='screenshot'){h+=\"<div><label>File Name</label>\"+inp(s.id,'value',s.value,'screenshot_name')+\"</div>\";}\n" +
            "  else if(a==='switchFrame'){h+=\"<div><label>Frame (XPath or Index)</label>\"+inp(s.id,'value',s.value,'//iframe  or  0')+\"</div>\";}\n" +
            "  else if(a==='getAttribute'){h+=\"<div><label>XPath</label>\"+inp(s.id,'xpath',s.xpath,'//input[@id=\\'x\\']')+\"</div>\";\n" +
            "    h+=\"<div><label>Attribute Name</label>\"+inp(s.id,'value',s.value,'value / href / class')+\"</div>\";}\n" +
            "  else if(a==='verifyText'){h+=\"<div><label>XPath</label>\"+inp(s.id,'xpath',s.xpath,'//h1')+\"</div>\";\n" +
            "    h+=\"<div><label>Expected Text</label>\"+inp(s.id,'value',s.value,'Welcome!')+\"</div>\";}\n" +
            "  else if(a==='dropdown'){h+=\"<div><label>XPath</label>\"+inp(s.id,'xpath',s.xpath,'//select[@id=\\'dd\\']')+\"</div>\";\n" +
            "    h+=\"<div><label>Select Method</label><select data-sid='\"+s.id+\"' data-fld='value'>\";\n" +
            "    ['selectByVisibleText','selectByIndex','selectByValue'].forEach(function(m){\n" +
            "      h+=\"<option value='\"+m+\"'\"+(s.value===m?' selected':'')+\">\"+m+\"</option>\";});\n" +
            "    h+=\"</select></div>\";\n" +
            "    h+=\"<div><label>\"+(s.value==='selectByIndex'?'Index (0,1,2...)':'Text / Value')+\"</label>\";\n" +
            "    h+=inp(s.id,'value2',s.value2,s.value==='selectByIndex'?'0':'Option text')+\"</div>\";}\n" +
            "  else{h+=\"<div><label>XPath</label>\"+inp(s.id,'xpath',s.xpath,'//button[@id=\\'submit\\']')+\"</div>\";\n" +
            "    if(a==='type')h+=\"<div><label>Text to Type</label>\"+inp(s.id,'value',s.value,'Enter text...')+\"</div>\";}\n" +
            "  h+=\"</div>\";\n" +
            "  h+=\"<div class='war'><label>&#x23F1; Wait After (ms)</label>\"+numInp(s.id,'wait',s.wait,'0')+\"</div>\";\n" +
            "  h+=\"<div class='sar'>\";\n" +
            "  h+=\"<button class='btn btn-sm btn-grn' data-act='save' data-id='\"+s.id+\"'>&#x2714; Save Step</button>\";\n" +
            "  h+=\"<span class='save-ok'>&#x2714; Saved!</span><div style='flex:1;'></div>\";\n" +
            "  h+=\"<button class='btn btn-sm' data-act='up' data-id='\"+s.id+\"'>&#x2191;</button>\";\n" +
            "  h+=\"<button class='btn btn-sm' data-act='dn' data-id='\"+s.id+\"'>&#x2193;</button>\";\n" +
            "  h+=\"<button class='btn btn-sm btn-red' data-act='del' data-id='\"+s.id+\"'>&#x1F5D1;</button></div>\";\n" +
            "  return h;\n" +
            "}\n" +
            "function updateCnt(){document.getElementById('scnt').textContent=steps.length+' step'+(steps.length!==1?'s':'');}\n" +
            "function getCfg(){\n" +
            "  var browsers=['chrome','firefox','edge'].filter(function(b){return document.getElementById('bc-'+b).classList.contains('on');});\n" +
            "  if(!browsers.length) browsers=['chrome'];\n" +
            "  return{url:document.getElementById('url').value||'https://example.com',browsers:browsers,\n" +
            "    maximize:document.getElementById('maximize').checked,headless:document.getElementById('headless').checked,\n" +
            "    resW:parseInt(document.getElementById('resW').value)||1920,resH:parseInt(document.getElementById('resH').value)||1080,\n" +
            "    implicitWait:parseInt(document.getElementById('implWait').value)||10,pageLoad:parseInt(document.getElementById('pageLoad').value)||30,\n" +
            "    autoScreenshot:document.getElementById('autoSS').checked,screenshotDir:document.getElementById('ssDir').value||'./screenshots',\n" +
            "    captureApi:document.getElementById('apiCapture').checked,steps:steps};\n" +
            "}\n" +
            "document.getElementById('btnRun').addEventListener('click',function(){\n" +
            "  if(!steps.length){toast('Add some steps first!','er');return;}\n" +
            "  document.getElementById('lp').innerHTML='';\n" +
            "  document.getElementById('alp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Capturing...</div>';\n" +
            "  document.getElementById('st').textContent='0';document.getElementById('sp').textContent='0';document.getElementById('sf').textContent='0';\n" +
            "  document.getElementById('pb').style.width='0%';document.getElementById('apiCnt').textContent='0';\n" +
            "  steps.forEach(function(s){s.status='pending';}); renderSteps();\n" +
            "  document.getElementById('btnRun').style.display='none';\n" +
            "  document.getElementById('btnStop').style.display='inline-flex';\n" +
            "  fetch('/run',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(getCfg())})\n" +
            "    .then(function(){startPolling();})\n" +
            "    .catch(function(e){toast('Server error: '+e.message,'er');resetButtons();});\n" +
            "});\n" +
            "document.getElementById('btnStop').addEventListener('click',function(){\n" +
            "  fetch('/stop',{method:'POST'}).then(function(){resetButtons();toast('Stopped','er');});\n" +
            "});\n" +
            "function startPolling(){\n" +
            "  if(polling) clearInterval(polling);\n" +
            "  polling=setInterval(pollStatus,600);\n" +
            "  if(apiPolling) clearInterval(apiPolling);\n" +
            "  apiPolling=setInterval(pollApiLogs,900);\n" +
            "}\n" +
            "function pollStatus(){\n" +
            "  fetch('/status').then(function(r){return r.json();}).then(function(d){\n" +
            "    document.getElementById('sp').textContent=d.pass;\n" +
            "    document.getElementById('sf').textContent=d.fail;\n" +
            "    document.getElementById('st').textContent=d.pass+d.fail;\n" +
            "    if(d.steps){Object.keys(d.steps).forEach(function(k){\n" +
            "      var idx=parseInt(k);\n" +
            "      if(steps[idx]){steps[idx].status=d.steps[k];\n" +
            "        var el=document.getElementById('sk-'+steps[idx].id);\n" +
            "        if(el)el.className='sk '+d.steps[k];}});}\n" +
            "    if(d.currentStep>=0&&steps[d.currentStep]){\n" +
            "      var cur=steps[d.currentStep],el=document.getElementById('sk-'+cur.id);\n" +
            "      if(el&&cur.status==='pending')el.className='sk run';\n" +
            "      document.getElementById('pb').style.width=((d.pass+d.fail)/steps.length*100)+'%';}\n" +
            "    if(!d.running){clearInterval(polling);polling=null;clearInterval(apiPolling);apiPolling=null;pollApiLogs();resetButtons();}\n" +
            "  }).catch(function(){});\n" +
            "}\n" +
            "setInterval(function(){\n" +
            "  fetch('/logs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var lp=document.getElementById('lp'); if(!logs.length) return; lp.innerHTML='';\n" +
            "    logs.forEach(function(l){\n" +
            "      var parts=l.split('|'); if(parts.length<3) return;\n" +
            "      var div=document.createElement('div'); div.className='ll';\n" +
            "      var msg=parts.slice(2).join('|');\n" +
            "      div.innerHTML='<span class=\"lt\">'+parts[0]+'</span><span class=\"lm '+parts[1]+'\">'+escH(msg)+'</span>';\n" +
            "      lp.appendChild(div);});\n" +
            "    lp.scrollTop=lp.scrollHeight;\n" +
            "  }).catch(function(){});\n" +
            "},700);\n" +
            "function pollApiLogs(){\n" +
            "  fetch('/apilogs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    document.getElementById('apiCnt').textContent=logs.length;\n" +
            "    var alp=document.getElementById('alp'); if(!logs.length) return; alp.innerHTML='';\n" +
            "    logs.forEach(function(l){\n" +
            "      var parts=l.split('|'); if(parts.length<3) return;\n" +
            "      var div=document.createElement('div'); div.className='ll';\n" +
            "      var msg=parts.slice(2).join('|');\n" +
            "      div.innerHTML='<span class=\"lt\">'+parts[0]+'</span><span class=\"lm '+parts[1]+'\">'+escH(msg)+'</span>';\n" +
            "      alp.appendChild(div);});\n" +
            "    alp.scrollTop=alp.scrollHeight;\n" +
            "  }).catch(function(){});\n" +
            "}\n" +
            "setInterval(pollApiLogs,1200);\n" +
            "function resetButtons(){document.getElementById('btnRun').style.display='inline-flex';document.getElementById('btnStop').style.display='none';}\n" +
            "document.getElementById('btnClearLogs').addEventListener('click',function(){\n" +
            "  fetch('/clearlogs',{method:'POST'}).then(function(){\n" +
            "    document.getElementById('lp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';\n" +
            "    toast('Execution log cleared','ok');});});\n" +
            "document.getElementById('btnClearApiLogs').addEventListener('click',function(){\n" +
            "  fetch('/clearapilogs',{method:'POST'}).then(function(){\n" +
            "    document.getElementById('alp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';\n" +
            "    document.getElementById('apiCnt').textContent='0';\n" +
            "    toast('API log cleared','ok');});});\n" +
            "document.getElementById('btnExportLogs').addEventListener('click',function(){\n" +
            "  fetch('/logs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var txt=logs.map(function(l){return l.replace(/\\|/g,' | ');}).join('\\n');\n" +
            "    downloadFile(txt,'exec_log_'+Date.now()+'.txt','text/plain');\n" +
            "    toast('Exec log exported','ok');});});\n" +
            "document.getElementById('btnExportApiLogs').addEventListener('click',function(){\n" +
            "  fetch('/apilogs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var txt=logs.map(function(l){return l.replace(/\\|/g,' | ');}).join('\\n');\n" +
            "    downloadFile(txt,'api_log_'+Date.now()+'.txt','text/plain');\n" +
            "    toast('API log exported (TXT)','ok');});});\n" +
            "document.getElementById('btnExportApiJson').addEventListener('click',function(){\n" +
            "  fetch('/apilogs').then(function(r){return r.json();}).then(function(logs){\n" +
            "    var parsed=logs.map(function(l){var p=l.split('|');return{time:p[0]||'',type:p[1]||'',message:p.slice(2).join('|')};});\n" +
            "    downloadFile(JSON.stringify(parsed,null,2),'api_log_'+Date.now()+'.json','application/json');\n" +
            "    toast('API log exported (JSON)','ok');});});\n" +
            "function downloadFile(content,filename,mime){\n" +
            "  var a=document.createElement('a');\n" +
            "  a.href=URL.createObjectURL(new Blob([content],{type:mime}));\n" +
            "  a.download=filename; a.click();\n" +
            "}\n" +
            "document.getElementById('btnCode').addEventListener('click',function(){\n" +
            "  fetch('/gencode',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(getCfg())})\n" +
            "    .then(function(r){return r.text();})\n" +
            "    .then(function(code){document.getElementById('codeDisp').textContent=code;document.getElementById('codeModal').classList.add('open');})\n" +
            "    .catch(function(){toast('Could not generate code','er');});\n" +
            "});\n" +
            "document.getElementById('btnCloseCode').addEventListener('click',function(){document.getElementById('codeModal').classList.remove('open');});\n" +
            "document.getElementById('btnCloseCode2').addEventListener('click',function(){document.getElementById('codeModal').classList.remove('open');});\n" +
            "document.getElementById('btnCopyCode').addEventListener('click',function(){\n" +
            "  navigator.clipboard.writeText(document.getElementById('codeDisp').textContent);\n" +
            "  toast('Copied! \\uD83D\\uDCCB','ok');\n" +
            "});\n" +
            "document.getElementById('btnSave').addEventListener('click',function(){\n" +
            "  downloadFile(JSON.stringify(getCfg(),null,2),'selenium_project_'+Date.now()+'.json','application/json');\n" +
            "  toast('Project saved!','ok');\n" +
            "});\n" +
            "document.getElementById('btnLoad').addEventListener('click',function(){\n" +
            "  var inp=document.createElement('input'); inp.type='file'; inp.accept='.json';\n" +
            "  inp.onchange=function(e){\n" +
            "    var r=new FileReader();\n" +
            "    r.onload=function(ev){\n" +
            "      try{\n" +
            "        var d=JSON.parse(ev.target.result);\n" +
            "        if(d.url) document.getElementById('url').value=d.url;\n" +
            "        if(d.browsers){['chrome','firefox','edge','all'].forEach(function(b){\n" +
            "          var c=document.getElementById('bc-'+b);\n" +
            "          d.browsers.includes(b)?c.classList.add('on'):c.classList.remove('on');});}\n" +
            "        if(d.maximize!==undefined) document.getElementById('maximize').checked=d.maximize;\n" +
            "        if(d.headless!==undefined) document.getElementById('headless').checked=d.headless;\n" +
            "        if(d.resW) document.getElementById('resW').value=d.resW;\n" +
            "        if(d.resH) document.getElementById('resH').value=d.resH;\n" +
            "        if(d.implicitWait) document.getElementById('implWait').value=d.implicitWait;\n" +
            "        if(d.pageLoad) document.getElementById('pageLoad').value=d.pageLoad;\n" +
            "        if(d.autoScreenshot!==undefined) document.getElementById('autoSS').checked=d.autoScreenshot;\n" +
            "        if(d.screenshotDir) document.getElementById('ssDir').value=d.screenshotDir;\n" +
            "        if(d.captureApi!==undefined) document.getElementById('apiCapture').checked=d.captureApi;\n" +
            "        if(d.steps){steps=d.steps.map(function(s){return Object.assign({status:'pending'},s);});\n" +
            "          ctr=steps.length?Math.max.apply(null,steps.map(function(s){return s.id;})):0;\n" +
            "          renderSteps();updateCnt();}\n" +
            "        toast('Project loaded!','ok');\n" +
            "      }catch(e){toast('Invalid file!','er');}\n" +
            "    };\n" +
            "    r.readAsText(e.target.files[0]);\n" +
            "  };\n" +
            "  inp.click();\n" +
            "});\n" +
            "function toast(msg,type){var t=document.getElementById('toast');t.textContent=msg;t.className='toast show '+(type||'');setTimeout(function(){t.className='toast';},2800);}\n" +
            "</script>\n";
    }
}
