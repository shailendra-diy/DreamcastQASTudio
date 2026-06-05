package com.selenium.studio;

public class HtmlBuilder {

    public static String build() {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Selenium Automation Studio v2.0</title>" +
            "<link href='https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;600&family=DM+Sans:wght@300;400;500;600;700&display=swap' rel='stylesheet'>" +
            buildCss() +
            "</head><body>" +
            buildApp() +
            buildCodeModal() +
            buildGroupModal() +
            "<div class='toast' id='toast'></div>" +
            buildScript() +
            "</body></html>";
    }

    private static String buildCss() {
        return "<style>" +
            ":root{--bg:#0a0c12;--s1:#111420;--s2:#181c2a;--s3:#1e2234;--bd:#252940;--bd2:#303558;" +
            "--acc:#4f8ef7;--acc2:#7c5ce4;--grn:#00e5a0;--red:#ff5270;--yel:#ffca3a;--ora:#ff9f43;" +
            "--tx:#dce1f0;--tx2:#8890aa;--tx3:#4a5070;" +
            "--mo:'JetBrains Mono',monospace;--sa:'DM Sans',sans-serif;}" +
            "*{margin:0;padding:0;box-sizing:border-box;}" +
            "body{font-family:var(--sa);background:var(--bg);color:var(--tx);height:100vh;display:flex;flex-direction:column;overflow:hidden;}" +
            "::-webkit-scrollbar{width:4px}::-webkit-scrollbar-track{background:transparent}::-webkit-scrollbar-thumb{background:var(--bd2);border-radius:4px}" +
            // Header
            ".hdr{background:var(--s1);border-bottom:1px solid var(--bd);padding:0 18px;height:52px;display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".logo{display:flex;align-items:center;gap:10px;}" +
            ".logo-icon{width:28px;height:28px;background:linear-gradient(135deg,var(--acc),var(--acc2));border-radius:7px;display:flex;align-items:center;justify-content:center;font-size:14px;}" +
            ".logo-txt{font-size:15px;font-weight:600;}" +
            ".logo-ver{font-size:10px;background:linear-gradient(135deg,var(--acc),var(--acc2));-webkit-background-clip:text;-webkit-text-fill-color:transparent;font-family:var(--mo);font-weight:600;}" +
            ".ha{display:flex;gap:6px;align-items:center;}" +
            // Buttons
            ".btn{padding:6px 13px;border-radius:7px;border:1px solid var(--bd2);background:transparent;color:var(--tx2);font-family:var(--sa);font-size:12px;font-weight:500;cursor:pointer;transition:all .15s;display:inline-flex;align-items:center;gap:5px;white-space:nowrap;}" +
            ".btn:hover{background:var(--s3);border-color:var(--acc);color:var(--acc);}" +
            ".btn-run{background:var(--grn);border-color:var(--grn);color:#000;font-weight:700;}" +
            ".btn-run:hover{background:#00d490;color:#000;}" +
            ".btn-stop{background:var(--red);border-color:var(--red);color:#fff;display:none;}" +
            ".btn-sm{padding:4px 9px;font-size:11px;}" +
            ".btn-acc{background:rgba(79,142,247,.15);border-color:var(--acc);color:var(--acc);}" +
            ".btn-acc:hover{background:rgba(79,142,247,.25);}" +
            ".btn-grn{background:rgba(0,229,160,.12);border-color:var(--grn);color:var(--grn);}" +
            ".btn-grn:hover{background:rgba(0,229,160,.22);}" +
            ".btn-red{border-color:var(--red);color:var(--red);}" +
            ".btn-red:hover{background:rgba(255,82,112,.1);}" +
            ".btn-ora{border-color:var(--ora);color:var(--ora);}" +
            ".btn-ora:hover{background:rgba(255,159,67,.1);}" +
            // Layout
            ".layout{display:flex;flex:1;overflow:hidden;}" +
            ".sidebar{width:272px;min-width:272px;background:var(--s1);border-right:1px solid var(--bd);overflow-y:auto;display:flex;flex-direction:column;}" +
            ".main{flex:1;display:flex;flex-direction:column;overflow:hidden;}" +
            ".rp{width:320px;min-width:320px;background:var(--s1);border-left:1px solid var(--bd);display:flex;flex-direction:column;}" +
            // Sidebar sections
            ".ph{padding:10px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;flex-shrink:0;}" +
            ".pt{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--tx3);}" +
            ".sec{padding:12px 13px;border-bottom:1px solid var(--bd);}" +
            ".sl{font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx3);margin-bottom:8px;}" +
            ".fl{margin-bottom:9px;}.fl:last-child{margin-bottom:0;}" +
            ".fl label{font-size:11px;color:var(--tx2);display:block;margin-bottom:4px;font-weight:500;}" +
            "input[type=text],input[type=number],select{width:100%;background:var(--s2);border:1px solid var(--bd2);color:var(--tx);border-radius:6px;padding:6px 9px;font-family:var(--sa);font-size:12px;outline:none;transition:border .15s;}" +
            "input:focus,select:focus{border-color:var(--acc);box-shadow:0 0 0 2px rgba(79,142,247,.1);}" +
            "select option{background:var(--s2);}" +
            // Browser grid
            ".bg{display:grid;grid-template-columns:1fr 1fr;gap:6px;}" +
            ".bc{background:var(--s2);border:1px solid var(--bd2);border-radius:7px;padding:8px;cursor:pointer;transition:all .15s;display:flex;align-items:center;gap:7px;user-select:none;}" +
            ".bc:hover{border-color:var(--acc);}" +
            ".bc.on{border-color:var(--acc);background:rgba(79,142,247,.08);}" +
            ".bi{font-size:17px;}.bn{font-size:11px;font-weight:600;}" +
            ".cd{width:12px;height:12px;border-radius:50%;border:2px solid var(--bd2);margin-left:auto;flex-shrink:0;transition:all .15s;}" +
            ".bc.on .cd{background:var(--acc);border-color:var(--acc);}" +
            // Toggles
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
            // Resolution
            ".r2{display:grid;grid-template-columns:1fr 1fr;gap:7px;}" +
            ".rr{display:flex;gap:6px;align-items:center;}.rr span{color:var(--tx3);font-size:11px;}.rr input{width:70px;}" +
            // Step toolbar
            ".stb{padding:9px 13px;border-bottom:1px solid var(--bd);display:flex;align-items:center;justify-content:space-between;background:var(--s1);flex-shrink:0;}" +
            ".sc{flex:1;overflow-y:auto;padding:9px;background:var(--bg);}" +
            // Step card
            ".sk{background:var(--s1);border:1px solid var(--bd);border-radius:9px;margin-bottom:8px;overflow:hidden;transition:border .2s;}" +
            ".sk:hover{border-color:var(--bd2);}" +
            ".sk.pass{border-left:3px solid var(--grn);}" +
            ".sk.fail{border-left:3px solid var(--red);}" +
            ".sk.run{border-left:3px solid var(--yel);animation:pulse 1s infinite;}" +
            ".sk.sel{border-color:var(--acc);background:rgba(79,142,247,.04);}" +
            "@keyframes pulse{0%,100%{opacity:1}50%{opacity:.65}}" +
            ".sh{padding:8px 11px;display:flex;align-items:center;gap:7px;cursor:pointer;}" +
            ".sn{width:20px;height:20px;border-radius:5px;background:var(--s3);display:flex;align-items:center;justify-content:center;font-size:9px;font-weight:700;color:var(--tx3);flex-shrink:0;}" +
            ".sk.pass .sn{background:rgba(0,229,160,.12);color:var(--grn);}" +
            ".sk.fail .sn{background:rgba(255,82,112,.12);color:var(--red);}" +
            ".sk.run .sn{background:rgba(255,202,58,.12);color:var(--yel);}" +
            ".sab{font-size:9px;font-weight:700;padding:2px 6px;border-radius:4px;background:rgba(79,142,247,.12);color:var(--acc);flex-shrink:0;text-transform:uppercase;letter-spacing:.5px;}" +
            ".snm{font-size:11px;color:var(--tx);font-weight:500;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}" +
            ".xbadge{font-size:9px;padding:1px 5px;border-radius:3px;background:rgba(255,159,67,.15);color:var(--ora);flex-shrink:0;}" +
            ".sb{padding:10px 11px;border-top:1px solid var(--bd);background:rgba(0,0,0,.15);display:none;}" +
            ".sb.open{display:block;}" +
            ".sf{display:grid;gap:7px;}" +
            // Multi xpath
            ".xrow{display:flex;gap:5px;align-items:center;margin-bottom:5px;}" +
            ".xrow input{flex:1;}" +
            ".xadd{font-size:10px;color:var(--grn);cursor:pointer;padding:3px 8px;border:1px solid var(--grn);border-radius:4px;background:transparent;display:inline-block;margin-top:2px;}" +
            ".xadd:hover{background:rgba(0,229,160,.1);}" +
            ".xdel{font-size:11px;color:var(--red);cursor:pointer;padding:2px 7px;border:1px solid var(--red);border-radius:4px;background:transparent;flex-shrink:0;}" +
            ".xdel:hover{background:rgba(255,82,112,.1);}" +
            // Step groups sidebar
            ".grp-card{background:var(--s2);border:1px solid var(--bd2);border-radius:7px;padding:8px 10px;margin-bottom:6px;cursor:pointer;transition:all .15s;}" +
            ".grp-card:hover{border-color:var(--acc2);}" +
            ".grp-title{font-size:12px;font-weight:600;color:var(--acc2);}" +
            ".grp-sub{font-size:10px;color:var(--tx3);margin-top:2px;}" +
            // Bottom
            ".war{display:flex;align-items:center;gap:7px;margin-top:7px;}" +
            ".war label{font-size:11px;color:var(--tx3);white-space:nowrap;}.war input{width:70px;}" +
            ".sar{display:flex;gap:6px;margin-top:9px;align-items:center;}" +
            ".save-ok{font-size:11px;color:var(--grn);display:none;}" +
            // Action panel
            ".asa{padding:10px 13px;border-top:1px solid var(--bd);background:var(--s1);display:none;flex-shrink:0;}" +
            ".ag{display:grid;grid-template-columns:repeat(3,1fr);gap:6px;margin-top:8px;}" +
            ".at{background:var(--s2);border:1px solid var(--bd);border-radius:7px;padding:8px 6px;text-align:center;cursor:pointer;transition:all .15s;}" +
            ".at:hover{border-color:var(--acc);background:rgba(79,142,247,.08);transform:translateY(-1px);}" +
            ".at .ic{font-size:17px;margin-bottom:2px;}.at .nm{font-size:9px;font-weight:600;color:var(--tx2);text-transform:uppercase;letter-spacing:.4px;}" +
            // Right panel
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
            // Tabs
            ".tabs{display:flex;border-bottom:1px solid var(--bd);flex-shrink:0;}" +
            ".tab{padding:8px 14px;font-size:11px;font-weight:600;color:var(--tx3);cursor:pointer;border-bottom:2px solid transparent;margin-bottom:-1px;transition:all .15s;display:flex;align-items:center;gap:5px;}" +
            ".tab:hover{color:var(--tx2);}.tab.active{color:var(--acc);border-bottom-color:var(--acc);}" +
            ".tab-pane{display:none;flex:1;overflow:hidden;flex-direction:column;}.tab-pane.active{display:flex;}" +
            ".ltb{padding:6px 9px;border-bottom:1px solid var(--bd);display:flex;gap:5px;align-items:center;flex-shrink:0;}" +
            ".api-badge{font-size:9px;padding:1px 5px;border-radius:3px;background:rgba(255,159,67,.15);color:var(--ora);}" +
            // Misc
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
            ".tag{font-size:10px;padding:2px 7px;border-radius:20px;}.tb{background:rgba(79,142,247,.12);color:var(--acc);}" +
            "</style>";
    }

    private static String buildApp() {
        return "<div style='display:flex;flex-direction:column;height:100vh;'>" +
            buildHeader() +
            "<div class='layout'>" +
            buildSidebar() +
            buildMain() +
            buildRightPanel() +
            "</div></div>";
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
            "<div class='ph'><span class='pt'>&#x2699; Configuration</span></div>" +

            // URL
            "<div class='sec'><div class='sl'>Target URL</div>" +
            "<div class='fl'><input type='text' id='url' placeholder='https://example.com'/></div></div>" +

            // Browser
            "<div class='sec'><div class='sl'>Browser</div><div class='bg'>" +
            "<div class='bc' id='bc-chrome' data-b='chrome'><div class='bi'>&#x1F310;</div><div class='bn'>Chrome</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-firefox' data-b='firefox'><div class='bi'>&#x1F98A;</div><div class='bn'>Firefox</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-edge' data-b='edge'><div class='bi'>&#x1F535;</div><div class='bn'>Edge</div><div class='cd'></div></div>" +
            "<div class='bc' id='bc-all' data-b='all'><div class='bi'>&#x26A1;</div><div class='bn'>All</div><div class='cd'></div></div>" +
            "</div></div>" +

            // Device presets
            "<div class='sec'><div class='sl'>Device / Resolution</div>" +
            "<div class='fl'><label>Device Preset</label>" +
            "<select id='devicePreset' onchange='applyPreset(this.value)'>" +
            "<option value=''>-- Custom --</option>" +
            "<optgroup label='Desktop'>" +
            "<option value='1920x1080'>Desktop HD (1920x1080)</option>" +
            "<option value='1366x768'>Desktop (1366x768)</option>" +
            "<option value='1280x800'>Laptop (1280x800)</option>" +
            "</optgroup>" +
            "<optgroup label='iPhone'>" +
            "<option value='390x844'>iPhone 13 (390x844)</option>" +
            "<option value='393x852'>iPhone 14 (393x852)</option>" +
            "<option value='430x932'>iPhone 14 Pro Max (430x932)</option>" +
            "<option value='393x852'>iPhone 15 (393x852)</option>" +
            "<option value='430x932'>iPhone 15 Pro Max (430x932)</option>" +
            "</optgroup>" +
            "<optgroup label='Samsung Galaxy'>" +
            "<option value='360x780'>Samsung S21 (360x780)</option>" +
            "<option value='360x780'>Samsung S22 (360x780)</option>" +
            "<option value='360x780'>Samsung S23 (360x780)</option>" +
            "<option value='360x780'>Samsung S24 (360x780)</option>" +
            "</optgroup>" +
            "<optgroup label='OnePlus'>" +
            "<option value='412x915'>OnePlus 9 (412x915)</option>" +
            "<option value='412x919'>OnePlus 10 Pro (412x919)</option>" +
            "<option value='412x915'>OnePlus 11 (412x915)</option>" +
            "<option value='412x915'>OnePlus 12 (412x915)</option>" +
            "</optgroup>" +
            "<optgroup label='Tablet'>" +
            "<option value='1024x1366'>iPad Pro (1024x1366)</option>" +
            "<option value='820x1180'>iPad Air (820x1180)</option>" +
            "</optgroup>" +
            "</select></div>" +
            "<div class='fl'><label>Resolution W x H</label>" +
            "<div class='rr'><input type='number' id='resW' value='1920'/><span>x</span><input type='number' id='resH' value='1080'/></div></div>" +
            "<div class='tr'><span class='tl'>&#x1F5A5; Maximize</span><label class='tg'><input type='checkbox' id='maximize' checked><span class='ts'></span></label></div>" +
            "<div class='tr'><span class='tl'>&#x1F47B; Headless</span><label class='tg'><input type='checkbox' id='headless'><span class='ts'></span></label></div>" +
            "</div>" +

            // Timeouts
            "<div class='sec'><div class='sl'>Timeouts</div><div class='r2'>" +
            "<div class='fl'><label>Implicit (sec)</label><input type='number' id='implWait' value='10'/></div>" +
            "<div class='fl'><label>Page Load (sec)</label><input type='number' id='pageLoad' value='30'/></div>" +
            "</div></div>" +

            // Screenshot
            "<div class='sec'><div class='sl'>Screenshot</div>" +
            "<div class='tr'><span class='tl'>&#x1F4F8; Auto on Fail</span><label class='tg'><input type='checkbox' id='autoSS' checked><span class='ts'></span></label></div>" +
            "<div class='fl' style='margin-top:6px;'><label>Save Folder</label><input type='text' id='ssDir' value='./screenshots'/></div></div>" +

            // API Capture
            "<div class='sec'><div class='sl'>API Capture</div>" +
            "<div class='tr'><span class='tl'><span id='apiLiveInd'></span>&#x1F4E1; Network Log</span>" +
            "<label class='tg tg-api'><input type='checkbox' id='apiCapture'><span class='ts'></span></label></div>" +
            "<div style='font-size:10px;color:var(--tx3);margin-top:4px;line-height:1.5;'>Chrome only. Firelink jaisa API log.</div></div>" +

            // Step Groups
            "<div class='sec'>" +
            "<div class='sl'>&#x1F4E6; Step Groups</div>" +
            "<div id='groupsList'><div style='font-size:11px;color:var(--tx3);padding:4px 0;'>Koi group nahi. Steps select karke group banao.</div></div>" +
            "</div>" +

            "</div>";
    }

    private static String buildMain() {
        return "<div class='main'>" +

            // Toolbar
            "<div class='stb'>" +
            "<div style='display:flex;align-items:center;gap:8px;'>" +
            "<span style='font-size:13px;font-weight:600;'>Test Steps</span>" +
            "<span class='tag tb' id='scnt'>0 steps</span>" +
            "</div>" +
            "<div style='display:flex;gap:6px;'>" +
            "<button class='btn btn-sm' id='btnClear'>&#x1F5D1; Clear</button>" +
            "<button class='btn btn-sm btn-ora' id='btnMakeGroup' style='display:none;' onclick='openGroupModal()'>&#x1F4E6; Group Banao</button>" +
            "<button class='btn btn-sm btn-acc' id='btnAddStep'>&#xFF0B; Add Step</button>" +
            "</div></div>" +

            // Steps container
            "<div class='sc' id='sc'>" +
            "<div class='es'><div class='ei'>&#x1F4CB;</div><p>Koi step nahi.<br><b>+ Add Step</b> dabao shuru karne ke liye.</p></div>" +
            "</div>" +

            // Action picker panel
            "<div class='asa' id='asa'>" +
            "<div style='display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;'>" +
            "<span style='font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.8px;color:var(--tx2);'>Action Choose Karo</span>" +
            "<button class='btn btn-sm' id='btnCloseAdd'>&#x2715;</button></div>" +
            "<div class='ag' id='actionGrid'>" +
            "<div class='at' data-action='click'><div class='ic'>&#x1F446;</div><div class='nm'>Click</div></div>" +
            "<div class='at' data-action='type'><div class='ic'>&#x2328;</div><div class='nm'>Type</div></div>" +
            "<div class='at' data-action='dropdown'><div class='ic'>&#x1F4CB;</div><div class='nm'>Dropdown</div></div>" +
            "<div class='at' data-action='getText'><div class='ic'>&#x1F4D6;</div><div class='nm'>Get Text</div></div>" +
            "<div class='at' data-action='getAttribute'><div class='ic'>&#x1F3F7;</div><div class='nm'>Get Attr</div></div>" +
            "<div class='at' data-action='verifyText'><div class='ic'>&#x2705;</div><div class='nm'>Verify Text</div></div>" +
            "<div class='at' data-action='verifyElement'><div class='ic'>&#x1F50D;</div><div class='nm'>Verify Elem</div></div>" +
            "<div class='at' data-action='navigate'><div class='ic'>&#x1F310;</div><div class='nm'>Navigate</div></div>" +
            "<div class='at' data-action='screenshot'><div class='ic'>&#x1F4F8;</div><div class='nm'>Screenshot</div></div>" +
            "<div class='at' data-action='scrollTo'><div class='ic'>&#x2195;</div><div class='nm'>Scroll</div></div>" +
            "<div class='at' data-action='hover'><div class='ic'>&#x1F5B1;</div><div class='nm'>Hover</div></div>" +
            "<div class='at' data-action='clearField'><div class='ic'>&#x1F5D1;</div><div class='nm'>Clear</div></div>" +
            "<div class='at' data-action='jsClick'><div class='ic'>&#x26A1;</div><div class='nm'>JS Click</div></div>" +
            "<div class='at' data-action='acceptAlert'><div class='ic'>&#x1F514;</div><div class='nm'>Alert</div></div>" +
            "<div class='at' data-action='switchFrame'><div class='ic'>&#x1F5BC;</div><div class='nm'>Frame</div></div>" +
            "<div class='at' data-action='switchWindow'><div class='ic'>&#x1F500;</div><div class='nm'>Window</div></div>" +
            "<div class='at' data-action='print'><div class='ic'>&#x1F4DD;</div><div class='nm'>Print Log</div></div>" +
            "<div class='at' data-action='wait'><div class='ic'>&#x23F3;</div><div class='nm'>Wait</div></div>" +
            "</div></div>" +
            "</div>";
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
            "<div class='tab active' data-tab='exec'>&#x1F4DC; Exec Log</div>" +
            "<div class='tab' data-tab='api'>&#x1F4E1; API Log <span class='api-badge' id='apiCnt'>0</span></div>" +
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
            "<button class='btn btn-sm btn-grn' id='btnExportApiLogs'>&#x1F4E4; TXT</button>" +
            "<button class='btn btn-sm btn-ora' id='btnExportApiJson'>JSON</button>" +
            "</div>" +
            "<div class='lp' id='alp'><div style='color:var(--tx3);font-size:11px;padding:14px;text-align:center;'>API Capture enable karo sidebar mein.</div></div>" +
            "</div></div>";
    }

    private static String buildCodeModal() {
        return "<div class='mo' id='codeModal'>" +
            "<div class='md' style='width:620px;'>" +
            "<div class='mh'><span class='mt'>&#x3C;/&#x3E; Generated Java Code</span>" +
            "<button class='mc' onclick='document.getElementById(\"codeModal\").classList.remove(\"open\")'>&#x2715;</button></div>" +
            "<div class='mb'><div style='font-size:11px;color:var(--tx2);margin-bottom:10px;'>Copy karke Eclipse mein paste karo as <b>GeneratedTest.java</b></div>" +
            "<div class='ca' id='codeDisp'></div></div>" +
            "<div class='mf'>" +
            "<button class='btn' onclick='navigator.clipboard.writeText(document.getElementById(\"codeDisp\").textContent);toast(\"Copied!\",\"ok\")'>&#x1F4CB; Copy</button>" +
            "<button class='btn btn-acc' onclick='document.getElementById(\"codeModal\").classList.remove(\"open\")'>Done</button>" +
            "</div></div></div>";
    }

    private static String buildGroupModal() {
        return "<div class='mo' id='groupModal'>" +
            "<div class='md' style='width:400px;'>" +
            "<div class='mh'><span class='mt'>&#x1F4E6; Step Group Banao</span>" +
            "<button class='mc' onclick='document.getElementById(\"groupModal\").classList.remove(\"open\")'>&#x2715;</button></div>" +
            "<div class='mb'>" +
            "<div id='grpSelInfo' style='font-size:12px;color:var(--tx2);margin-bottom:14px;padding:8px 12px;background:var(--s2);border-radius:6px;'></div>" +
            "<div class='fl'><label>Group Ka Naam</label>" +
            "<input type='text' id='groupName' placeholder='Jaise: Login Flow, Checkout Steps, Search'/></div>" +
            "</div>" +
            "<div class='mf'>" +
            "<button class='btn' onclick='document.getElementById(\"groupModal\").classList.remove(\"open\")'>Cancel</button>" +
            "<button class='btn btn-acc' onclick='saveGroup()'>&#x1F4BE; Save Group</button>" +
            "</div></div></div>";
    }

    private static String buildScript() {
        return "<script>\n" +
            "var steps=[], ctr=0, polling=null, stepGroups=[];\n" +
            "var LABELS={click:'Click',type:'Type',dropdown:'Dropdown',getText:'Get Text',getAttribute:'Get Attr',verifyText:'Verify Text',verifyElement:'Verify Elem',navigate:'Navigate',screenshot:'Screenshot',scrollTo:'Scroll',hover:'Hover',clearField:'Clear',jsClick:'JS Click',acceptAlert:'Alert',switchFrame:'Frame',switchWindow:'Window',print:'Print',wait:'Wait'};\n" +

            // Device preset
            "function applyPreset(v){\n" +
            "  if(!v) return;\n" +
            "  var p=v.split('x');\n" +
            "  if(p.length!==2) return;\n" +
            "  document.getElementById('resW').value=p[0];\n" +
            "  document.getElementById('resH').value=p[1];\n" +
            "  var mob=parseInt(p[0])<768;\n" +
            "  document.getElementById('maximize').checked=!mob;\n" +
            "  toast('Device: '+v,'ok');\n" +
            "}\n" +

            // Tabs
            "document.querySelectorAll('.tab').forEach(function(t){\n" +
            "  t.addEventListener('click',function(){\n" +
            "    document.querySelectorAll('.tab').forEach(x=>x.classList.remove('active'));\n" +
            "    document.querySelectorAll('.tab-pane').forEach(x=>x.classList.remove('active'));\n" +
            "    t.classList.add('active');\n" +
            "    document.getElementById('pane-'+t.dataset.tab).classList.add('active');\n" +
            "  });\n" +
            "});\n" +

            // Browser toggle
            "document.querySelectorAll('.bc').forEach(function(el){\n" +
            "  el.addEventListener('click',function(){\n" +
            "    var b=el.dataset.b;\n" +
            "    if(b==='all'){var w=!el.classList.contains('on');['chrome','firefox','edge'].forEach(x=>{document.getElementById('bc-'+x).classList[w?'add':'remove']('on');});el.classList[w?'add':'remove']('on');}\n" +
            "    else{el.classList.toggle('on');var s=['chrome','firefox','edge'].filter(x=>document.getElementById('bc-'+x).classList.contains('on'));document.getElementById('bc-all').classList[s.length===3?'add':'remove']('on');}\n" +
            "  });\n" +
            "});\n" +

            // API capture toggle
            "document.getElementById('apiCapture').addEventListener('change',function(){\n" +
            "  fetch('/setapicapture',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({enabled:this.checked})});\n" +
            "  document.getElementById('apiLiveInd').innerHTML=this.checked?'<span style=\"display:inline-block;width:6px;height:6px;border-radius:50%;background:var(--ora);margin-right:3px;animation:blink 1s infinite;\"></span>':'';\n" +
            "  toast(this.checked?'API Capture ON':'API Capture OFF',this.checked?'ok':'');\n" +
            "});\n" +

            // Add step panel
            "document.getElementById('btnAddStep').addEventListener('click',function(){\n" +
            "  var a=document.getElementById('asa');\n" +
            "  a.style.display=(a.style.display==='block')?'none':'block';\n" +
            "});\n" +
            "document.getElementById('btnCloseAdd').addEventListener('click',function(){document.getElementById('asa').style.display='none';});\n" +

            // Action grid click
            "document.getElementById('actionGrid').addEventListener('click',function(e){\n" +
            "  var at=e.target.closest('.at'); if(!at) return;\n" +
            "  var action=at.dataset.action; if(!action) return;\n" +
            "  ctr++;\n" +
            "  steps.push({id:ctr,name:'',action:action,xpaths:[''],value:'',value2:'',wait:0,status:'pending',sel:false});\n" +
            "  renderSteps(); updateCnt();\n" +
            "  setTimeout(function(){var b=document.getElementById('sb-'+ctr);if(b)b.classList.add('open');var k=document.getElementById('sk-'+ctr);if(k)k.scrollIntoView({behavior:'smooth',block:'nearest'});},50);\n" +
            "  document.getElementById('asa').style.display='none';\n" +
            "  toast('Added: '+LABELS[action],'ok');\n" +
            "});\n" +

            // Clear all
            "document.getElementById('btnClear').addEventListener('click',function(){\n" +
            "  if(steps.length&&!confirm('Saare steps delete karo?')) return;\n" +
            "  steps=[]; renderSteps(); updateCnt(); updateGroupBtn();\n" +
            "});\n" +

            // Step list delegation
            "document.getElementById('sc').addEventListener('click',function(e){\n" +
            "  if(e.target.classList.contains('step-cb')){\n" +
            "    var cb=e.target;\n" +
            "    var id=parseInt(cb.dataset.id);\n" +
            "    var s=steps.find(x=>x.id===id);\n" +
            "    if(s){s.sel=cb.checked;}\n" +
            "    var sk=document.getElementById('sk-'+id);\n" +
            "    if(sk)sk.classList[cb.checked?'add':'remove']('sel');\n" +
            "    updateGroupBtn();\n" +
            "    return;\n" +
            "  }\n" +
            "  var btn=e.target.closest('button[data-act]');\n" +
            "  var sh=e.target.closest('.sh');\n" +
            "  if(sh&&!btn){var sk=sh.closest('.sk');if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.toggle('open');}return;}\n" +
            "  if(!btn) return;\n" +
            "  var act=btn.dataset.act,id=parseInt(btn.dataset.id);\n" +
            "  if(act==='del'){steps=steps.filter(s=>s.id!==id);renderSteps();updateCnt();updateGroupBtn();}\n" +
            "  else if(act==='up'||act==='dn'){var i=steps.findIndex(s=>s.id===id),n=act==='up'?i-1:i+1;if(n>=0&&n<steps.length){var t=steps[i];steps[i]=steps[n];steps[n]=t;}renderSteps();setTimeout(()=>{var sk=document.getElementById('sk-'+id);if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.add('open');}},50);}\n" +
            "  else if(act==='save'){var sk=document.getElementById('sk-'+id);if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.remove('open');var ok=sk.querySelector('.save-ok');if(ok){ok.style.display='inline';setTimeout(()=>ok.style.display='none',1800);}}toast('Saved','ok');}\n" +
            "  else if(act==='xadd'){var s=steps.find(x=>x.id===id);if(s){s.xpaths.push('');renderSteps();setTimeout(()=>{var sk=document.getElementById('sk-'+id);if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.add('open');}},50);}}\n" +
            "  else if(act==='xdel'){var xi=parseInt(btn.dataset.xi);var s=steps.find(x=>x.id===id);if(s&&s.xpaths.length>1){s.xpaths.splice(xi,1);renderSteps();setTimeout(()=>{var sk=document.getElementById('sk-'+id);if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.add('open');}},50);}}\n" +
            "});\n" +

            // Field update
            "document.getElementById('sc').addEventListener('input',function(e){\n" +
            "  var el=e.target,id=parseInt(el.dataset.sid),fld=el.dataset.fld;\n" +
            "  if(!id||!fld) return;\n" +
            "  var s=steps.find(x=>x.id===id); if(!s) return;\n" +
            "  if(fld==='xpath'){var xi=parseInt(el.dataset.xi||0);if(!s.xpaths)s.xpaths=[''];s.xpaths[xi]=el.value;}\n" +
            "  else s[fld]=el.value;\n" +
            "  var nm=document.querySelector('#sk-'+id+' .snm');\n" +
            "  if(nm)nm.textContent=s.name||(s.xpaths&&s.xpaths[0])||s.value||'Configure...';\n" +
            "});\n" +
            "document.getElementById('sc').addEventListener('change',function(e){\n" +
            "  var el=e.target,id=parseInt(el.dataset.sid),fld=el.dataset.fld;\n" +
            "  if(!id||!fld) return;\n" +
            "  var s=steps.find(x=>x.id===id); if(!s) return;\n" +
            "  if(fld==='xpath'){var xi=parseInt(el.dataset.xi||0);if(!s.xpaths)s.xpaths=[''];s.xpaths[xi]=el.value;}\n" +
            "  else s[fld]=el.value;\n" +
            "  if(fld==='value'&&s.action==='dropdown'){renderSteps();setTimeout(()=>{var sk=document.getElementById('sk-'+id);if(sk){var sb=sk.querySelector('.sb');if(sb)sb.classList.add('open');}},50);}\n" +
            "});\n" +

            // Render steps
            "function renderSteps(){\n" +
            "  var c=document.getElementById('sc');\n" +
            "  if(!steps.length){c.innerHTML=\"<div class='es'><div class='ei'>&#x1F4CB;</div><p>Koi step nahi.<br><b>+ Add Step</b> dabao.</p></div>\";return;}\n" +
            "  var h='';\n" +
            "  steps.forEach(function(s,i){\n" +
            "    var ico=s.status==='pass'?'&#x2705;':s.status==='fail'?'&#x274C;':s.status==='run'?'&#x23F3;':'';\n" +
            "    var xpaths=s.xpaths||[s.xpath||''];\n" +
            "    var xcount=xpaths.filter(x=>x).length;\n" +
            "    h+=\"<div class='sk \"+(s.sel?'sel':'')+\" \"+s.status+\"' id='sk-\"+s.id+\"'>\";\n" +
            "    h+=\"<div class='sh'>\";\n" +
            "    h+=\"<input type='checkbox' class='step-cb' data-id='\"+s.id+\"' \"+(s.sel?'checked':'')+\" style='accent-color:var(--acc);cursor:pointer;flex-shrink:0;width:14px;height:14px;'>\";\n" +

            "    h+=\"<div class='sn'>\"+(i+1)+\"</div>\";\n" +
            "    h+=\"<div class='sab'>\"+(LABELS[s.action]||s.action)+\"</div>\";\n" +
            "    h+=\"<div class='snm'>\"+escH(s.name||(xpaths[0])||s.value||'Configure...')+\"</div>\";\n" +
            "    if(xcount>1)h+=\"<span class='xbadge'>+\"+(xcount-1)+\" xpath</span>\";\n" +
            "    h+=\"<span style='font-size:15px;margin-left:4px;flex-shrink:0;'>\"+ico+\"</span></div>\";\n" +
            "    h+=\"<div class='sb' id='sb-\"+s.id+\"'>\"+renderFields(s)+\"</div>\";\n" +
            "    h+=\"</div>\";\n" +
            "  });\n" +
            "  c.innerHTML=h;\n" +
            "}\n" +

            "function escH(s){return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');}\n" +
            "function inp(sid,fld,val,ph){return \"<input type='text' data-sid='\"+sid+\"' data-fld='\"+fld+\"' value='\"+escH(val)+\"' placeholder='\"+escH(ph)+\"'/>\";}\n" +
            "function numInp(sid,fld,val,ph){return \"<input type='number' data-sid='\"+sid+\"' data-fld='\"+fld+\"' value='\"+escH(String(val))+\"' placeholder='\"+escH(ph)+\"'/>\";}\n" +

            "function renderXpaths(s){\n" +
            "  var xpaths=s.xpaths&&s.xpaths.length?s.xpaths:[''];\n" +
            "  var h=\"<div><label>XPath (multiple — fallback chain)</label>\";\n" +
            "  xpaths.forEach(function(xp,xi){\n" +
            "    h+=\"<div class='xrow'>\";\n" +
            "    h+=\"<input type='text' data-sid='\"+s.id+\"' data-fld='xpath' data-xi='\"+xi+\"' value='\"+escH(xp)+\"' placeholder='XPath \"+(xi+1)+(xi===0?' (primary)':xi===1?' (fallback 1)':' (fallback '+(xi)+'))')+\"'/>\";\n" +
            "    if(xi>0)h+=\"<button class='xdel' data-act='xdel' data-id='\"+s.id+\"' data-xi='\"+xi+\"'>&#x2715;</button>\";\n" +
            "    h+=\"</div>\";\n" +
            "  });\n" +
            "  h+=\"<button class='xadd' data-act='xadd' data-id='\"+s.id+\"'>+ Fallback XPath Add Karo</button>\";\n" +
            "  h+=\"</div>\";\n" +
            "  return h;\n" +
            "}\n" +

            "function renderFields(s){\n" +
            "  var h=\"<div class='sf'>\";\n" +
            "  h+=\"<div><label>Step Ka Naam</label>\"+inp(s.id,'name',s.name,'Login karo, Search karo, Checkout...')+\"</div>\";\n" +
            "  var a=s.action;\n" +
            "  if(a==='navigate')h+=\"<div><label>URL</label>\"+inp(s.id,'value',s.value,'https://...')+\"</div>\";\n" +
            "  else if(a==='print')h+=\"<div><label>Message</label>\"+inp(s.id,'value',s.value,'Log message...')+\"</div>\";\n" +
            "  else if(a==='wait')h+=\"<div><label>Duration (ms)</label>\"+numInp(s.id,'value',s.value||1000,'1000')+\"</div>\";\n" +
            "  else if(a==='acceptAlert'){h+=\"<div><label>Action</label><select data-sid='\"+s.id+\"' data-fld='value'><option value='accept'\"+(s.value!=='dismiss'?' selected':'')+\">Accept (OK)</option><option value='dismiss'\"+(s.value==='dismiss'?' selected':'')+\">Dismiss (Cancel)</option></select></div>\";}\n" +
            "  else if(a==='screenshot')h+=\"<div><label>File Name</label>\"+inp(s.id,'value',s.value,'screenshot_name')+\"</div>\";\n" +
            "  else if(a==='switchFrame')h+=\"<div><label>Frame Index ya XPath</label>\"+inp(s.id,'value',s.value,'0 ya //iframe')+\"</div>\";\n" +
            "  else if(a==='getAttribute'){h+=renderXpaths(s);h+=\"<div><label>Attribute</label>\"+inp(s.id,'value',s.value,'value / href / class')+\"</div>\";}\n" +
            "  else if(a==='verifyText'){h+=renderXpaths(s);h+=\"<div><label>Expected Text</label>\"+inp(s.id,'value',s.value,'Welcome!')+\"</div>\";}\n" +
            "  else if(a==='dropdown'){h+=renderXpaths(s);h+=\"<div><label>Method</label><select data-sid='\"+s.id+\"' data-fld='value'>\";['selectByVisibleText','selectByIndex','selectByValue'].forEach(function(m){h+=\"<option value='\"+m+\"'\"+(s.value===m?' selected':'')+\">\"+m+\"</option>\";});h+=\"</select></div>\";h+=\"<div><label>\"+(s.value==='selectByIndex'?'Index (0,1...)':'Text ya Value')+\"</label>\"+inp(s.id,'value2',s.value2,'Option text')+\"</div>\";}\n" +
            "  else{h+=renderXpaths(s);if(a==='type')h+=\"<div><label>Text to Type</label>\"+inp(s.id,'value',s.value,'Enter text...')+\"</div>\";}\n" +
            "  h+=\"</div>\";\n" +
            "  h+=\"<div class='war'><label>&#x23F1; Wait After (ms)</label>\"+numInp(s.id,'wait',s.wait,'0')+\"</div>\";\n" +
            "  h+=\"<div class='sar'>\";\n" +
            "  h+=\"<button class='btn btn-sm btn-grn' data-act='save' data-id='\"+s.id+\"'>&#x2714; Save Step</button>\";\n" +
            "  h+=\"<span class='save-ok'>&#x2714; Saved!</span><div style='flex:1'></div>\";\n" +
            "  h+=\"<button class='btn btn-sm' data-act='up' data-id='\"+s.id+\"'>&#x2191;</button>\";\n" +
            "  h+=\"<button class='btn btn-sm' data-act='dn' data-id='\"+s.id+\"'>&#x2193;</button>\";\n" +
            "  h+=\"<button class='btn btn-sm btn-red' data-act='del' data-id='\"+s.id+\"'>&#x1F5D1;</button></div>\";\n" +
            "  return h;\n" +
            "}\n" +

            "function updateCnt(){document.getElementById('scnt').textContent=steps.length+' step'+(steps.length!==1?'s':'');}\n" +

"function updateGroupBtn(){\n" +
"  var n=steps.filter(function(s){return s.sel;}).length;\n" +
"  var b=document.getElementById('btnMakeGroup');\n" +
"  if(n>0){\n" +
"    b.style.display='inline-flex';\n" +
"    b.textContent='\\uD83D\\uDCE6 Group Banao ('+n+')';\n" +
"  } else {\n" +
"    b.style.display='none';\n" +
"  }\n" +
"}\n" +
            // Step Groups
            "function openGroupModal(){\n" +
            "  var n=steps.filter(s=>s.sel).length;\n" +
            "  document.getElementById('grpSelInfo').textContent=n+' steps selected hain is group mein';\n" +
            "  document.getElementById('groupName').value='';\n" +
            "  document.getElementById('groupModal').classList.add('open');\n" +
            "}\n" +

            "function saveGroup(){\n" +
            "  var name=document.getElementById('groupName').value.trim();\n" +
            "  if(!name){toast('Group ka naam daalo!','er');return;}\n" +
            "  var sel=steps.filter(s=>s.sel);\n" +
            "  if(!sel.length){toast('Koi step select nahi','er');return;}\n" +
            "  var g={id:'g'+Date.now(),name:name,steps:JSON.parse(JSON.stringify(sel))};\n" +
            "  stepGroups.push(g);\n" +
            "  steps.forEach(s=>s.sel=false);\n" +
            "  renderSteps(); renderGroups(); updateGroupBtn();\n" +
            "  document.getElementById('groupModal').classList.remove('open');\n" +
            "  toast('Group saved: '+name,'ok');\n" +
            "}\n" +
            "function renderGroups(){\n" +
            "  var c=document.getElementById('groupsList');\n" +
            "  if(!stepGroups.length){\n" +
            "    c.innerHTML='<div style=\"font-size:11px;color:var(--tx3);padding:4px 0;\">Koi group nahi. Steps select karke group banao.</div>';\n" +
            "    return;\n" +
            "  }\n" +
            "  var h='';\n" +
            "  stepGroups.forEach(function(g,gi){\n" +
            "    h+=\"<div style='background:var(--s2);border:1px solid var(--bd2);border-radius:7px;margin-bottom:6px;overflow:hidden;'>\";\n" +
            "    h+=\"<div style='padding:8px 10px;display:flex;align-items:center;gap:8px;cursor:pointer;' onclick='toggleGroupCard(\\\"grpc-\"+gi+\"\\\")'>\";\n" +
            "    h+=\"<span style='font-size:13px;'>&#x1F4E6;</span>\";\n" +
            "    h+=\"<span style='font-size:12px;font-weight:600;color:var(--acc2);flex:1;'>\"+escH(g.name)+\"</span>\";\n" +
            "    h+=\"<span style='font-size:10px;color:var(--tx3);'>\"+g.steps.length+\" steps</span>\";\n" +
            "    h+=\"<span id='grpa-\"+gi+\"' style='font-size:10px;color:var(--tx3);margin-left:4px;'>&#x25BC;</span>\";\n" +
            "    h+=\"</div>\";\n" +
            "    h+=\"<div id='grpc-\"+gi+\"' style='display:none;border-top:1px solid var(--bd);padding:8px 10px;'>\";\n" +
            "    h+=\"<button onclick='insertGroup(\\\"\"+g.id+\"\\\")' style='width:100%;padding:6px;background:var(--acc);border:none;border-radius:6px;color:#fff;font-size:11px;font-weight:600;cursor:pointer;margin-bottom:6px;'>&#x2795; Steps Add Karo</button>\";\n" +
            "    g.steps.forEach(function(s,si){\n" +
            "      h+=\"<div style='font-size:11px;color:var(--tx2);padding:3px 0;border-bottom:1px solid rgba(37,41,64,.5);display:flex;align-items:center;gap:6px;'>\";\n" +
            "      h+=\"<span style='font-size:9px;background:rgba(79,142,247,.12);color:var(--acc);padding:1px 5px;border-radius:3px;'>\"+escH((LABELS[s.action]||s.action))+\"</span>\";\n" +
            "      h+=\"<span style='overflow:hidden;text-overflow:ellipsis;white-space:nowrap;'>\"+escH(s.name||(s.xpaths&&s.xpaths[0])||s.value||'Step '+(si+1))+\"</span>\";\n" +
            "      h+=\"</div>\";\n" +
            "    });\n" +
            "    h+=\"<button onclick='deleteGroup(\\\"\"+g.id+\"\\\")' style='width:100%;padding:4px;background:transparent;border:1px solid var(--red);border-radius:6px;color:var(--red);font-size:10px;cursor:pointer;margin-top:6px;'>&#x1F5D1; Delete Group</button>\";\n" +
            "    h+=\"</div></div>\";\n" +
            "  });\n" +
            "  c.innerHTML=h;\n" +
            "}\n" +
            "function toggleGroupCard(id){\n" +
            "  var c=document.getElementById(id);\n" +
            "  var gi=id.replace('grpc-','');\n" +
            "  var arrow=document.getElementById('grpa-'+gi);\n" +
            "  if(!c) return;\n" +
            "  if(c.style.display==='none'||c.style.display===''){\n" +
            "    c.style.display='block';\n" +
            "    if(arrow)arrow.innerHTML='&#x25B2;';\n" +
            "  } else {\n" +
            "    c.style.display='none';\n" +
            "    if(arrow)arrow.innerHTML='&#x25BC;';\n" +
            "  }\n" +
            "}\n" +

            "function deleteGroup(gid){\n" +
            "  if(!confirm('Ye group delete karo?')) return;\n" +
            "  stepGroups=stepGroups.filter(function(g){return g.id!==gid;});\n" +
            "  renderGroups();\n" +
            "  toast('Group deleted','ok');\n" +
            "}\n" +

"function insertGroup(gid){\n" +
"  var g=null;\n" +
"  for(var i=0;i<stepGroups.length;i++){if(stepGroups[i].id===gid){g=stepGroups[i];break;}}\n" +
"  if(!g){toast('Group nahi mila','er');return;}\n" +
"  for(var j=0;j<g.steps.length;j++){\n" +
"    ctr++;\n" +
"    var gs=g.steps[j];\n" +
"    var ns={\n" +
"      id:ctr,\n" +
"      name:gs.name||'',\n" +
"      action:gs.action||'click',\n" +
"      xpaths:gs.xpaths?gs.xpaths.slice():[''],\n" +
"      value:gs.value||'',\n" +
"      value2:gs.value2||'',\n" +
"      wait:gs.wait||0,\n" +
"      status:'pending',\n" +
"      sel:false\n" +
"    };\n" +
"    steps.push(ns);\n" +
"  }\n" +
"  renderSteps();\n" +
"  updateCnt();\n" +
"  toast(g.steps.length+' steps add ho gaye: '+g.name,'ok');\n" +
"}\n" +

            // Run/Stop
            "document.getElementById('btnRun').addEventListener('click',function(){\n" +
            "  if(!steps.length){toast('Pehle steps add karo!','er');return;}\n" +
            "  document.getElementById('lp').innerHTML='';\n" +
            "  document.getElementById('alp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Capturing...</div>';\n" +
            "  ['st','sp','sf'].forEach(id=>document.getElementById(id).textContent='0');\n" +
            "  document.getElementById('pb').style.width='0%';\n" +
            "  document.getElementById('apiCnt').textContent='0';\n" +
            "  steps.forEach(s=>s.status='pending'); renderSteps();\n" +
            "  document.getElementById('btnRun').style.display='none';\n" +
            "  document.getElementById('btnStop').style.display='inline-flex';\n" +
            "  fetch('/run',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(getCfg())})\n" +
            "    .then(()=>startPolling())\n" +
            "    .catch(e=>{toast('Error: '+e.message,'er');resetButtons();});\n" +
            "});\n" +
            "document.getElementById('btnStop').addEventListener('click',function(){\n" +
            "  fetch('/stop',{method:'POST'}).then(()=>{resetButtons();toast('Stopped','er');});\n" +
            "});\n" +

            "function startPolling(){if(polling)clearInterval(polling);polling=setInterval(pollStatus,600);}\n" +

            "function pollStatus(){\n" +
            "  fetch('/status').then(r=>r.json()).then(function(d){\n" +
            "    document.getElementById('sp').textContent=d.pass;\n" +
            "    document.getElementById('sf').textContent=d.fail;\n" +
            "    document.getElementById('st').textContent=d.pass+d.fail;\n" +
            "    if(d.steps){Object.keys(d.steps).forEach(k=>{var i=parseInt(k);if(steps[i]){steps[i].status=d.steps[k];var el=document.getElementById('sk-'+steps[i].id);if(el){el.className='sk '+d.steps[k]+(steps[i].sel?' sel':'');}}});}\n" +
            "    if(d.currentStep>=0&&steps[d.currentStep]){var c=steps[d.currentStep];var el=document.getElementById('sk-'+c.id);if(el&&c.status==='pending')el.className='sk run';document.getElementById('pb').style.width=((d.pass+d.fail)/steps.length*100)+'%';}\n" +
            "    if(!d.running){clearInterval(polling);polling=null;pollApiLogs();resetButtons();}\n" +
            "  }).catch(()=>{});\n" +
            "}\n" +

            "setInterval(function(){\n" +
            "  fetch('/logs').then(r=>r.json()).then(function(logs){\n" +
            "    if(!logs.length)return;\n" +
            "    var lp=document.getElementById('lp');lp.innerHTML='';\n" +
            "    logs.forEach(function(l){var p=l.split('|');if(p.length<3)return;var d=document.createElement('div');d.className='ll';d.innerHTML='<span class=\"lt\">'+p[0]+'</span><span class=\"lm '+p[1]+'\">'+escH(p.slice(2).join('|'))+'</span>';lp.appendChild(d);});\n" +
            "    lp.scrollTop=lp.scrollHeight;\n" +
            "  }).catch(()=>{});\n" +
            "},700);\n" +

            "function pollApiLogs(){\n" +
            "  fetch('/apilogs').then(r=>r.json()).then(function(logs){\n" +
            "    document.getElementById('apiCnt').textContent=logs.length;\n" +
            "    if(!logs.length)return;\n" +
            "    var alp=document.getElementById('alp');alp.innerHTML='';\n" +
            "    logs.forEach(function(l){var p=l.split('|');if(p.length<3)return;var d=document.createElement('div');d.className='ll';d.innerHTML='<span class=\"lt\">'+p[0]+'</span><span class=\"lm '+p[1]+'\">'+escH(p.slice(2).join('|'))+'</span>';alp.appendChild(d);});\n" +
            "    alp.scrollTop=alp.scrollHeight;\n" +
            "  }).catch(()=>{});\n" +
            "}\n" +
            "setInterval(pollApiLogs,1200);\n" +
            "function resetButtons(){document.getElementById('btnRun').style.display='inline-flex';document.getElementById('btnStop').style.display='none';}\n" +

            // Logs
            "document.getElementById('btnClearLogs').addEventListener('click',function(){fetch('/clearlogs',{method:'POST'}).then(()=>{document.getElementById('lp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';toast('Log cleared','ok');});});\n" +
            "document.getElementById('btnClearApiLogs').addEventListener('click',function(){fetch('/clearapilogs',{method:'POST'}).then(()=>{document.getElementById('alp').innerHTML='<div style=\"color:var(--tx3);font-size:11px;padding:14px;text-align:center;\">Cleared.</div>';document.getElementById('apiCnt').textContent='0';toast('API log cleared','ok');});});\n" +
            "document.getElementById('btnExportLogs').addEventListener('click',function(){fetch('/logs').then(r=>r.json()).then(logs=>{dl(logs.map(l=>l.replace(/\\|/g,' | ')).join('\\n'),'exec_log_'+Date.now()+'.txt','text/plain');toast('Exported','ok');});});\n" +
            "document.getElementById('btnExportApiLogs').addEventListener('click',function(){fetch('/apilogs').then(r=>r.json()).then(logs=>{dl(logs.map(l=>l.replace(/\\|/g,' | ')).join('\\n'),'api_log_'+Date.now()+'.txt','text/plain');toast('API TXT exported','ok');});});\n" +
            "document.getElementById('btnExportApiJson').addEventListener('click',function(){fetch('/apilogs').then(r=>r.json()).then(logs=>{var p=logs.map(l=>{var a=l.split('|');return{time:a[0]||'',type:a[1]||'',message:a.slice(2).join('|')};});dl(JSON.stringify(p,null,2),'api_log_'+Date.now()+'.json','application/json');toast('JSON exported','ok');});});\n" +
            "function dl(c,f,m){var a=document.createElement('a');a.href=URL.createObjectURL(new Blob([c],{type:m}));a.download=f;a.click();}\n" +

            // Code modal
            "document.getElementById('btnCode').addEventListener('click',function(){fetch('/gencode',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(getCfg())}).then(r=>r.text()).then(code=>{document.getElementById('codeDisp').textContent=code;document.getElementById('codeModal').classList.add('open');}).catch(()=>toast('Code error','er'));});\n" +

            // Save/Load project
            "document.getElementById('btnSave').addEventListener('click',function(){\n" +
            "  dl(JSON.stringify(getCfg(),null,2),'selenium_project_'+Date.now()+'.json','application/json');\n" +
            "  toast('Project saved!','ok');\n" +
            "});\n" +
            "document.getElementById('btnLoad').addEventListener('click',function(){\n" +
            "  var fi=document.createElement('input');fi.type='file';fi.accept='.json';\n" +
            "  fi.onchange=function(e){\n" +
            "    var r=new FileReader();\n" +
            "    r.onload=function(ev){\n" +
            "      try{\n" +
            "        var d=JSON.parse(ev.target.result);\n" +
            "        if(d.url)document.getElementById('url').value=d.url;\n" +
            "        if(d.browsers){['chrome','firefox','edge','all'].forEach(b=>{document.getElementById('bc-'+b).classList[d.browsers.includes(b)?'add':'remove']('on');});}\n" +
            "        if(d.maximize!==undefined)document.getElementById('maximize').checked=d.maximize;\n" +
            "        if(d.headless!==undefined)document.getElementById('headless').checked=d.headless;\n" +
            "        if(d.resW)document.getElementById('resW').value=d.resW;\n" +
            "        if(d.resH)document.getElementById('resH').value=d.resH;\n" +
            "        if(d.implicitWait)document.getElementById('implWait').value=d.implicitWait;\n" +
            "        if(d.pageLoad)document.getElementById('pageLoad').value=d.pageLoad;\n" +
            "        if(d.autoScreenshot!==undefined)document.getElementById('autoSS').checked=d.autoScreenshot;\n" +
            "        if(d.screenshotDir)document.getElementById('ssDir').value=d.screenshotDir;\n" +
            "        if(d.captureApi!==undefined)document.getElementById('apiCapture').checked=d.captureApi;\n" +
            "        if(d.steps){steps=d.steps.map(s=>Object.assign({status:'pending',sel:false,xpaths:s.xpaths||[s.xpath||'']},s));ctr=steps.length?Math.max(...steps.map(s=>s.id)):0;renderSteps();updateCnt();}\n" +
            "        if(d.stepGroups){stepGroups=d.stepGroups;renderGroups();}\n" +
            "        toast('Project loaded!','ok');\n" +
            "      }catch(ex){toast('Invalid file!','er');}\n" +
            "    };r.readAsText(e.target.files[0]);\n" +
            "  };fi.click();\n" +
            "});\n" +

            "function toast(msg,type){var t=document.getElementById('toast');t.textContent=msg;t.className='toast show '+(type||'');setTimeout(()=>t.className='toast',2800);}\n" +
            "</script>\n";
    }
}