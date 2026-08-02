// SHADOWX-FB WebView Injection Script

(function() {
    console.log('SHADOWX-FB: Injection started');
    
    // Remove ads
    function removeAds() {
        const adSelectors = [
            '.ads', '.ad', '.advertisement', '.sponsored',
            '[class*="ad"]', '[id*="ad"]', '[class*="sponsored"]',
            '[data-ad]', '[data-ads]', '[data-sponsored]'
        ];
        
        adSelectors.forEach(selector => {
            document.querySelectorAll(selector).forEach(el => {
                el.style.display = 'none';
                el.remove();
            });
        });
    }
    
    // Add download buttons
    function addDownloadButtons() {
        const links = document.querySelectorAll('a[href*=".apk"], a[href*=".zip"], a[href*=".pdf"]');
        links.forEach(link => {
            if (!link.querySelector('.download-btn')) {
                const btn = document.createElement('button');
                btn.className = 'download-btn';
                btn.innerHTML = '📥 Download';
                btn.style.cssText = `
                    background: #1877F2;
                    color: white;
                    border: none;
                    padding: 5px 10px;
                    border-radius: 5px;
                    cursor: pointer;
                    margin-left: 10px;
                `;
                btn.onclick = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    window.location.href = 'shadowxfb://download/' + link.href;
                };
                link.parentNode.insertBefore(btn, link.nextSibling);
            }
        });
    }
    
    // Messenger support
    function setupMessenger() {
        const messengerLinks = document.querySelectorAll('a[href*="messenger.com"], a[href*="m.me"]');
        messengerLinks.forEach(link => {
            link.target = '_self';
            link.onclick = function(e) {
                window.location.href = this.href;
                return false;
            };
        });
    }
    
    // Dark mode toggle
    function toggleDarkMode() {
        if (document.body.classList.contains('dark-mode')) {
            document.body.classList.remove('dark-mode');
        } else {
            document.body.classList.add('dark-mode');
        }
    }
    
    // Initialize
    function init() {
        removeAds();
        addDownloadButtons();
        setupMessenger();
        
        // Watch for DOM changes
        const observer = new MutationObserver(() => {
            removeAds();
            addDownloadButtons();
            setupMessenger();
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }
    
    // Run after page load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
    
    console.log('SHADOWX-FB: Injection complete');
})();